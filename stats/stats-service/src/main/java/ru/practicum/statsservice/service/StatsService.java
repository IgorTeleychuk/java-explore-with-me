package ru.practicum.statsservice.service;


import ru.practicum.statsdto.model.EndpointHit;
import ru.practicum.statsdto.model.ViewStats;

import java.time.LocalDateTime;
import java.util.List;

public interface StatsService {
    void addHit(EndpointHit endpointHit);

    List<ViewStats> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique);
}
