package ru.practicum.ewmservice.participation.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewmservice.participation.dto.EventRequestDto;
import ru.practicum.ewmservice.participation.service.EventRequestService;

import javax.validation.constraints.Positive;
import java.util.List;

@RestController
@RequestMapping(path = "/users/{userId}/requests")
@RequiredArgsConstructor
@Validated
public class EventRequestPrivateController {
    private final EventRequestService eventRequestService;

    @PostMapping
    @ResponseStatus(value = HttpStatus.CREATED)
    public EventRequestDto create(@PathVariable("userId") @Positive long userId,
                                  @RequestParam(name = "eventId") @Positive long eventId) {
        return eventRequestService.create(userId, eventId);
    }

    @GetMapping
    public List<EventRequestDto> getAll(@PathVariable("userId") @Positive long userId) {
        return eventRequestService.getAll(userId);
    }

    @PatchMapping("/{requestId}/cancel")
    public EventRequestDto cansel(@PathVariable("userId") @Positive long userId,
                                  @PathVariable(name = "requestId") @Positive long requestId) {
        return eventRequestService.cansel(userId, requestId);
    }
}
