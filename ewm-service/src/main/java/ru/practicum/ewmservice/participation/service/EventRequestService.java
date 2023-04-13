package ru.practicum.ewmservice.participation.service;

import ru.practicum.ewmservice.participation.dto.EventRequestDto;

import java.util.List;

public interface EventRequestService {
    EventRequestDto create(long userId, long eventId);

    List<EventRequestDto> getAll(long userId);

    EventRequestDto cansel(long userId, long requestId);
}
