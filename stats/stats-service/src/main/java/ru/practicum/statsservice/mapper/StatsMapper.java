package ru.practicum.statsservice.mapper;


import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.statsdto.model.EndpointHit;
import ru.practicum.statsservice.model.Stats;

import java.time.LocalDateTime;

@Mapper(componentModel = "spring")
public interface StatsMapper {
    @Mapping(target = "timestamp", expression = "java(timestamp)")
    Stats toStats(EndpointHit endpointHit, LocalDateTime timestamp);
}
