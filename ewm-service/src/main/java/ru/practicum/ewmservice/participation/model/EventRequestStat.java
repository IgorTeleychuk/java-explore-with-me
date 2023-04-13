package ru.practicum.ewmservice.participation.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(name = "event_request_stats")
@Setter
@Getter
public class EventRequestStat {
    @Id
    @Column(name = "stat_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "name")
    private String name;
}
