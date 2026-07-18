package com.ggoulart.doodle.meeting.persistence;

import com.ggoulart.doodle.meeting.application.MeetingRepository;
import com.ggoulart.doodle.meeting.domain.Meeting;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
class JpaMeetingRepository implements MeetingRepository {

    private final MeetingJpaRepository meetingJpaRepository;

    JpaMeetingRepository(MeetingJpaRepository meetingJpaRepository) {
        this.meetingJpaRepository = meetingJpaRepository;
    }

    @Override
    public Meeting save(Meeting meeting) {
        MeetingEntity entity = new MeetingEntity(
                meeting.id(), meeting.slotId(), meeting.title(), meeting.description(), meeting.participants());
        MeetingEntity saved = meetingJpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public boolean existsBySlotId(UUID slotId) {
        return meetingJpaRepository.existsBySlotId(slotId);
    }

    private Meeting toDomain(MeetingEntity entity) {
        return new Meeting(entity.getId(), entity.getSlotId(), entity.getTitle(), entity.getDescription(), entity.getParticipants());
    }
}
