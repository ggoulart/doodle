package com.ggoulart.doodle.meeting.persistence;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "meetings")
class MeetingEntity {

    @Id
    private UUID id;

    @Column(name = "slot_id", unique = true)
    private UUID slotId;

    private String title;

    private String description;

    @ElementCollection
    @CollectionTable(
            name = "meeting_participants",
            joinColumns = @JoinColumn(name = "meeting_id"),
            uniqueConstraints = @UniqueConstraint(columnNames = {"meeting_id", "participant"}))
    @Column(name = "participant")
    private List<String> participants;

    protected MeetingEntity() {
    }

    MeetingEntity(UUID id, UUID slotId, String title, String description, List<String> participants) {
        this.id = id;
        this.slotId = slotId;
        this.title = title;
        this.description = description;
        this.participants = participants != null ? participants : List.of();
    }

    UUID getId() {
        return id;
    }

    UUID getSlotId() {
        return slotId;
    }

    String getTitle() {
        return title;
    }

    String getDescription() {
        return description;
    }

    List<String> getParticipants() {
        return participants;
    }
}
