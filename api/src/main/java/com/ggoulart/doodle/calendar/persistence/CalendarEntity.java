package com.ggoulart.doodle.calendar.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "calendars")
class CalendarEntity {

    @Id
    private UUID id;

    @Column(name = "user_id", unique = true)
    private UUID userId;

    protected CalendarEntity() {
    }

    CalendarEntity(UUID id, UUID userId) {
        this.id = id;
        this.userId = userId;
    }

    UUID getId() {
        return id;
    }

    UUID getUserId() {
        return userId;
    }
}
