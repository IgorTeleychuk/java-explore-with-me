package ru.practicum.ewmservice.event.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewmservice.event.dto.EventFullDto;
import ru.practicum.ewmservice.event.dto.EventIncomeDto;
import ru.practicum.ewmservice.event.dto.EventSorts;
import ru.practicum.ewmservice.event.model.AdminComment;
import ru.practicum.ewmservice.event.model.Event;
import ru.practicum.ewmservice.event.model.EventStates;
import ru.practicum.ewmservice.participation.model.EventRequest;
import ru.practicum.ewmservice.util.UtilService;
import ru.practicum.ewmservice.util.mappers.EventMapper;
import ru.practicum.ewmservice.util.storage.EventQFilter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class EventAdminServiceImpl implements EventAdminService {
    private final UtilService utilService;
    private final EventSuperService eventService;

    @Override
    @Transactional
    public EventFullDto update(EventIncomeDto dto, long eventId) {
        Map<Long, Integer> views;
        List<EventRequest> confirmedRequests;
        Map<Long, AdminComment> comments;
        eventService.checkEventDate(dto, 1);
        Event event = utilService.findEventOrThrow(eventId);

        event = eventService.update(event, dto);
        confirmedRequests = utilService.findConfirmedRequests(event);
        views = utilService.findViews(eventId);

        comments = eventService.saveAdminComment(List.of(dto), List.of(event));

        log.info("Updated Event with Id = {} administrator", eventId);
        return EventMapper.toEventFullDto(event, confirmedRequests, views, comments);
    }

    @Override
    public List<EventFullDto> getAll(List<Long> userIds,
                                     List<EventStates> states,
                                     List<Long> categories,
                                     LocalDateTime rangeStart,
                                     LocalDateTime rangeEnd,
                                     int from, int size) {
        List<Event> events;
        Map<Long, Integer> views;
        Map<Event, List<EventRequest>> confirmedRequests;
        Map<Long, AdminComment> comments;

        Pageable pageable = eventService.createPageableBySort(EventSorts.EVENT_DATE, from, size);
        EventQFilter filter = fillFilter(userIds, states, categories, rangeStart, rangeEnd, null);

        events = utilService.findByFilter(pageable, filter);
        views = utilService.findViews(events);
        confirmedRequests = utilService.findConfirmedRequests(events);
        comments = utilService.findByEventId(events);

        log.info("Returned a list of events at the request of the administrator");
        return EventMapper.toEventFullDto(events, confirmedRequests, views, comments);
    }

    private EventQFilter fillFilter(List<Long> userIds,
                                    List<EventStates> states,
                                    List<Long> categories,
                                    LocalDateTime rangeStart,
                                    LocalDateTime rangeEnd,
                                    List<Long> eventIds) {
        return EventQFilter.builder()
                .userIds(userIds)
                .states(states == null ? null : states.stream().map(Enum::name).collect(Collectors.toList()))
                .rangeStart(rangeStart)
                .rangeEnd(rangeEnd)
                .categories(categories)
                .eventIds(eventIds)
                .build();
    }
}
