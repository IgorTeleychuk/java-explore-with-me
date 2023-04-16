package ru.practicum.ewmservice.event.model;

import lombok.Getter;
import lombok.Setter;
import ru.practicum.ewmservice.categories.model.Category;
import ru.practicum.ewmservice.compilation.model.Compilation;
import ru.practicum.ewmservice.user.model.User;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "events")
@Setter
@Getter
public class Event {
    @Id
    @Column(name = "event_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "annotation")
    private String annotation; // Short description
    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "category_id")
    private Category category;
    @Column(name = "created_on")
    private LocalDateTime createdOn; // Date and time the event was created (format "yyyy-MM-dd HH:mm:ss")
    @Column(name = "description")
    private String description; // Full description
    @Column(name = "event_date")
    private LocalDateTime eventDate; // The date and time for which the event is scheduled (format "yyyy-MM-dd HH:mm:ss")
    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "initiator_id")
    private User initiator; // Создатель
    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "location_id")
    private Location location; //Coordinates of the venue
    @Column(name = "paid")
    private Boolean paid; // Do I need to pay for participation
    @Column(name = "participant_limit")
    private Integer participantLimit; // Limit on the number of participants. A value of 0 means that there is no restriction
    @Column(name = "published_on")
    private LocalDateTime publishedOn; // Date and time of publication of the event (format "yyyy-MM-dd HH:mm:ss")
    @Column(name = "request_moderation")
    private Boolean requestModeration; // Do I need early moderation of applications for participation
    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "state_id")
    private EventState state;// List of event lifecycle states
    @Column(name = "title")
    private String title; // Title
    @ManyToMany(mappedBy = "events")
    private Set<Compilation> compilations; // Compilations
}
