package com.ggoulart.doodle.slot.persistence;

import com.ggoulart.doodle.slot.domain.SlotStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "slots",
        indexes = @Index(name = "idx_slots_calendar_id_start_time_end_time", columnList = "calendar_id, start_time, end_time"))
class SlotEntity {

    @Id
    private UUID id;

    @Column(name = "calendar_id")
    private UUID calendarId;

    @Column(name = "start_time")
    private Instant startTime;

    @Column(name = "end_time")
    private Instant endTime;

    @Enumerated(EnumType.STRING)
    private SlotStatus status;

    protected SlotEntity() {
    }

    SlotEntity(UUID id, UUID calendarId, Instant startTime, Instant endTime, SlotStatus status) {
        this.id = id;
        this.calendarId = calendarId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
    }

    UUID getId() {
        return id;
    }

    UUID getCalendarId() {
        return calendarId;
    }

    Instant getStartTime() {
        return startTime;
    }

    Instant getEndTime() {
        return endTime;
    }

    SlotStatus getStatus() {
        return status;
    }
}
