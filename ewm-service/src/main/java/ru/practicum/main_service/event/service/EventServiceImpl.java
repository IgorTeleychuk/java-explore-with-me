package ru.practicum.main_service.event.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.main_service.category.model.Category;
import ru.practicum.main_service.category.service.CategoryService;
import ru.practicum.main_service.event.dto.EventFullDto;
import ru.practicum.main_service.event.dto.EventShortDto;
import ru.practicum.main_service.event.dto.LocationDto;
import ru.practicum.main_service.event.dto.NewEventDto;
import ru.practicum.main_service.event.dto.UpdateEventAdminRequest;
import ru.practicum.main_service.event.dto.UpdateEventUserRequest;
import ru.practicum.main_service.event.enums.EventSortType;
import ru.practicum.main_service.event.enums.EventState;
import ru.practicum.main_service.event.mapper.EventMapper;
import ru.practicum.main_service.event.mapper.LocationMapper;
import ru.practicum.main_service.event.model.Event;
import ru.practicum.main_service.event.model.Location;
import ru.practicum.main_service.event.repository.EventRepository;
import ru.practicum.main_service.event.repository.LocationRepository;
import ru.practicum.main_service.exception.ForbiddenException;
import ru.practicum.main_service.exception.NotFoundException;
import ru.practicum.main_service.user.model.User;
import ru.practicum.main_service.user.service.UserService;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class EventServiceImpl implements EventService {
    private final UserService userService;
    private final CategoryService categoryService;
    private final StatsService statsService;
    private final LocationRepository locationRepository;
    private final EventRepository eventRepository;
    private final EventMapper eventMapper;
    private final LocationMapper locationMapper;

    @Override
    public List<EventFullDto> getEventsByAdmin(List<Long> users, List<EventState> states, List<Long> categories,
                                               LocalDateTime rangeStart, LocalDateTime rangeEnd, Integer from, Integer size) {
        log.info("Output of events to the administrator's request with the users parameters = {}, states = {}, categoriesId = {}, " +
                "rangeStart = {}, rangeEnd = {}, from = {}, size = {}",
                users, states, categories, rangeStart, rangeEnd, from, size);

        checkStartIsBeforeEnd(rangeStart, rangeEnd);

        List<Event> events = eventRepository.getEventsByAdmin(users, states, categories, rangeStart, rangeEnd, from, size);

        return toEventsFullDto(events);
    }

    @Override
    @Transactional
    public EventFullDto patchEventByAdmin(Long eventId, UpdateEventAdminRequest updateEventAdminRequest) {
            log.info("Updating an event with an id {} at the request of the administrator with the parameters {}", eventId, updateEventAdminRequest);

        checkNewEventDate(updateEventAdminRequest.getEventDate(), LocalDateTime.now().plusHours(1));

        Event event = getEventById(eventId);

        if (updateEventAdminRequest.getAnnotation() != null) {
            event.setAnnotation(updateEventAdminRequest.getAnnotation());
        }

        if (updateEventAdminRequest.getDescription() != null) {
            event.setDescription(updateEventAdminRequest.getDescription());
        }

        if (updateEventAdminRequest.getCategory() != null) {
            event.setCategory(categoryService.getCategoryById(updateEventAdminRequest.getCategory()));
        }

        if (updateEventAdminRequest.getEventDate() != null) {
            event.setEventDate(updateEventAdminRequest.getEventDate());
        }

        if (updateEventAdminRequest.getPaid() != null) {
            event.setPaid(updateEventAdminRequest.getPaid());
        }

        if (updateEventAdminRequest.getLocation() != null) {
            event.setLocation(getOrSaveLocation(updateEventAdminRequest.getLocation()));
        }

        if (updateEventAdminRequest.getParticipantLimit() != null) {
            checkIsNewLimitNotLessOld(updateEventAdminRequest.getParticipantLimit(),
                    statsService.getConfirmedRequests(List.of(event)).getOrDefault(eventId, 0L));

            event.setParticipantLimit(updateEventAdminRequest.getParticipantLimit());
        }

        if (updateEventAdminRequest.getRequestModeration() != null) {
            event.setRequestModeration(updateEventAdminRequest.getRequestModeration());
        }

        if (updateEventAdminRequest.getStateAction() != null) {
            if (!event.getState().equals(EventState.PENDING)) {
                    throw new ForbiddenException(String.format("Field: stateAction. Error: you can only publish " +
                        "events pending publication. Current status: %s", event.getState()));
            }

            switch (updateEventAdminRequest.getStateAction()) {
                case PUBLISH_EVENT:
                    event.setState(EventState.PUBLISHED);
                    event.setPublishedOn(LocalDateTime.now());
                    break;
                case REJECT_EVENT:
                    event.setState(EventState.REJECTED);
                    break;
            }
        }

        if (updateEventAdminRequest.getTitle() != null) {
            event.setTitle(updateEventAdminRequest.getTitle());
        }

        return toEventFullDto(eventRepository.save(event));
    }

    @Override
    public List<EventShortDto> getAllEventsByPrivate(Long userId, Pageable pageable) {
        log.info("Output of all user events with id {} and pagination {}", userId, pageable);

        userService.getUserById(userId);

        List<Event> events = eventRepository.findAllByInitiatorId(userId, pageable);

        return toEventsShortDto(events);
    }

    @Override
    @Transactional
    public EventFullDto createEventByPrivate(Long userId, NewEventDto newEventDto) {
        log.info("Creating a new event by a user with id {} and parameters{}", userId, newEventDto);

        checkNewEventDate(newEventDto.getEventDate(), LocalDateTime.now().plusHours(2));

        User eventUser = userService.getUserById(userId);
        Category eventCategory = categoryService.getCategoryById(newEventDto.getCategory());
        Location eventLocation = getOrSaveLocation(newEventDto.getLocation());

        Event newEvent = eventMapper.toEvent(newEventDto, eventUser, eventCategory, eventLocation, LocalDateTime.now(),
                EventState.PENDING);

        return toEventFullDto(eventRepository.save(newEvent));
    }

    @Override
    public EventFullDto getEventByPrivate(Long userId, Long eventId) {
        log.info("Event output with id {}, created by a user with id {}", eventId, userId);

        userService.getUserById(userId);

        Event event = getEventByIdAndInitiatorId(eventId, userId);

        return toEventFullDto(eventRepository.save(event));
    }

    @Override
    @Transactional
    public EventFullDto patchEventByPrivate(Long userId, Long eventId, UpdateEventUserRequest updateEventUserRequest) {
        log.info("Event update with id {} at the request of the user with id {} with new parameters {}",
                eventId, userId, updateEventUserRequest);

        checkNewEventDate(updateEventUserRequest.getEventDate(), LocalDateTime.now().plusHours(2));

        userService.getUserById(userId);

        Event event = getEventByIdAndInitiatorId(eventId, userId);

        if (event.getState().equals(EventState.PUBLISHED)) {
            throw new ForbiddenException("You can only change unpublished or canceled events.");
        }

        if (updateEventUserRequest.getAnnotation() != null) {
            event.setAnnotation(updateEventUserRequest.getAnnotation());
        }

        if (updateEventUserRequest.getCategory() != null) {
            event.setCategory(categoryService.getCategoryById(updateEventUserRequest.getCategory()));
        }

        if (updateEventUserRequest.getDescription() != null) {
            event.setDescription(updateEventUserRequest.getDescription());
        }

        if (updateEventUserRequest.getEventDate() != null) {
            event.setEventDate(updateEventUserRequest.getEventDate());
        }

        if (updateEventUserRequest.getLocation() != null) {
            event.setLocation(getOrSaveLocation(updateEventUserRequest.getLocation()));
        }

        if (updateEventUserRequest.getPaid() != null) {
            event.setPaid(updateEventUserRequest.getPaid());
        }

        if (updateEventUserRequest.getParticipantLimit() != null) {
            event.setParticipantLimit(updateEventUserRequest.getParticipantLimit());
        }

        if (updateEventUserRequest.getRequestModeration() != null) {
            event.setRequestModeration(updateEventUserRequest.getRequestModeration());
        }

        if (updateEventUserRequest.getStateAction() != null) {
            switch (updateEventUserRequest.getStateAction()) {
                case SEND_TO_REVIEW:
                    event.setState(EventState.PENDING);
                    break;
                case CANCEL_REVIEW:
                    event.setState(EventState.CANCELED);
                    break;
            }
        }

        if (updateEventUserRequest.getTitle() != null) {
            event.setTitle(updateEventUserRequest.getTitle());
        }

        return toEventFullDto(eventRepository.save(event));
    }

    @Override
    public List<EventShortDto> getEventsByPublic(
            String text, List<Long> categories, Boolean paid, LocalDateTime rangeStart, LocalDateTime rangeEnd,
            Boolean onlyAvailable, EventSortType sort, Integer from, Integer size, HttpServletRequest request) {
        log.info("Output of events to a public request with parameters text = {}, categoriesId = {}, paid = {}, rangeStart = {}, " +
                "rangeEnd = {}, onlyAvailable = {}, sort = {}, from = {}, size = {}",
                text, categories, paid, rangeStart, rangeEnd, onlyAvailable, sort, from, size);

        checkStartIsBeforeEnd(rangeStart, rangeEnd);

        List<Event> events = eventRepository.getEventsByPublic(text, categories, paid, rangeStart, rangeEnd, from, size);

        if (events.isEmpty()) {
            return List.of();
        }

        Map<Long, Integer> eventsParticipantLimit = new HashMap<>();
        events.forEach(event -> eventsParticipantLimit.put(event.getId(), event.getParticipantLimit()));

        List<EventShortDto> eventsShortDto = toEventsShortDto(events);

        if (onlyAvailable) {
            eventsShortDto = eventsShortDto.stream()
                    .filter(eventShort -> (eventsParticipantLimit.get(eventShort.getId()) == 0 ||
                            eventsParticipantLimit.get(eventShort.getId()) > eventShort.getConfirmedRequests()))
                    .collect(Collectors.toList());
        }

        if (needSort(sort, EventSortType.VIEWS)) {
            eventsShortDto.sort(Comparator.comparing(EventShortDto::getViews));
        } else if (needSort(sort, EventSortType.EVENT_DATE)) {
            eventsShortDto.sort(Comparator.comparing(EventShortDto::getEventDate));
        }

        statsService.addHit(request);

        return eventsShortDto;
    }

    @Override
    public EventFullDto getEventByPublic(Long eventId, HttpServletRequest request) {
        log.info("Event output with id {} to a public inquiry", eventId);

        Event event = getEventById(eventId);

        if (!event.getState().equals(EventState.PUBLISHED)) {
            throw new NotFoundException("The event with this id has not been published.");
        }

        statsService.addHit(request);

        return toEventFullDto(event);
    }

    @Override
    public Event getEventById(Long eventId) {
        log.info("Event output with id {}", eventId);

        return eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("There is no event with this id."));
    }

    @Override
    public List<Event> getEventsByIds(List<Long> eventsId) {
        log.info("Output a list of events with ids {}", eventsId);

        if (eventsId.isEmpty()) {
            return new ArrayList<>();
        }

        return eventRepository.findAllByIdIn(eventsId);
    }

    @Override
    public List<EventShortDto> toEventsShortDto(List<Event> events) {
        log.info("Converting a list of events to an EventShortDto {}", events);

        Map<Long, Long> views = statsService.getViews(events);
        Map<Long, Long> confirmedRequests = statsService.getConfirmedRequests(events);

        return events.stream()
                .map((event) -> eventMapper.toEventShortDto(
                        event,
                        confirmedRequests.getOrDefault(event.getId(), 0L),
                        views.getOrDefault(event.getId(), 0L)))
                .collect(Collectors.toList());
    }

    private List<EventFullDto> toEventsFullDto(List<Event> events) {
        Map<Long, Long> views = statsService.getViews(events);
        Map<Long, Long> confirmedRequests = statsService.getConfirmedRequests(events);

        return events.stream()
                .map((event) -> eventMapper.toEventFullDto(
                        event,
                        confirmedRequests.getOrDefault(event.getId(), 0L),
                        views.getOrDefault(event.getId(), 0L)))
                .collect(Collectors.toList());
    }

    private EventFullDto toEventFullDto(Event event) {
        return toEventsFullDto(List.of(event)).get(0);
    }

    private Event getEventByIdAndInitiatorId(Long eventId, Long userId) {
        log.info("Event output with id {}", eventId);

        return eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException("Event with this id don't exist."));
    }

    private Location getOrSaveLocation(LocationDto locationDto) {
        Location newLocation = locationMapper.toLocation(locationDto);
        return locationRepository.findByLatAndLon(newLocation.getLat(), newLocation.getLon())
                .orElseGet(() -> locationRepository.save(newLocation));
    }

    private Boolean needSort(EventSortType sort, EventSortType typeToCompare) {
        return sort != null && sort.equals(typeToCompare);
    }

    private void checkStartIsBeforeEnd(LocalDateTime rangeStart, LocalDateTime rangeEnd) {
        if (rangeStart != null && rangeEnd != null && rangeStart.isAfter(rangeEnd)) {
            throw new ForbiddenException(String.format("Field: eventDate. Error: incorrect parameters of the temporary " +
                    "interval. Value: rangeStart = %s, rangeEnd = %s", rangeStart, rangeEnd));
        }
    }

    private void checkNewEventDate(LocalDateTime newEventDate, LocalDateTime minTimeBeforeEventStart) {
        if (newEventDate != null && newEventDate.isBefore(minTimeBeforeEventStart)) {
            throw new ForbiddenException(String.format("Field: eventDate. Error: there is too little time left for " +
                            "preparation. Value: %s", newEventDate));
        }
    }

    private void checkIsNewLimitNotLessOld(Integer newLimit, Long eventParticipantLimit) {
        if (newLimit != 0 && eventParticipantLimit != 0 && (newLimit < eventParticipantLimit)) {
            throw new ForbiddenException(String.format("Field: stateAction. Error: The new limit of participants must " +
                    "be no less than the number of applications already approved: %s", eventParticipantLimit));
        }
    }
}
