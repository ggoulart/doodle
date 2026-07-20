package com.ggoulart.doodle.meeting.persistence;

import com.ggoulart.doodle.meeting.application.MeetingAlreadyExistsException;
import com.ggoulart.doodle.meeting.application.MeetingRepository;
import com.ggoulart.doodle.meeting.domain.InvalidParticipantException;
import com.ggoulart.doodle.meeting.domain.Meeting;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Repository;

@Repository
class JpaMeetingRepository implements MeetingRepository {

    private static final String SLOT_ID_UNIQUE_CONSTRAINT = "uk_meetings_slot_id";
    private static final String PARTICIPANTS_UNIQUE_CONSTRAINT = "uk_meeting_participants";

    private final MeetingJpaRepository meetingJpaRepository;

    JpaMeetingRepository(MeetingJpaRepository meetingJpaRepository) {
        this.meetingJpaRepository = meetingJpaRepository;
    }

    @Override
    public Meeting save(Meeting meeting) {
        MeetingEntity entity = new MeetingEntity(
                meeting.id(), meeting.slotId(), meeting.title(), meeting.description(), meeting.participants());
        try {
            MeetingEntity saved = meetingJpaRepository.saveAndFlush(entity);
            return toDomain(saved);
        } catch (DataIntegrityViolationException ex) {
            if (violatesConstraint(ex, SLOT_ID_UNIQUE_CONSTRAINT)) {
                throw new MeetingAlreadyExistsException(meeting.slotId());
            }
            if (violatesConstraint(ex, PARTICIPANTS_UNIQUE_CONSTRAINT)) {
                throw new InvalidParticipantException("participants must not contain duplicates");
            }
            throw ex;
        }
    }

    @Override
    public Optional<Meeting> findById(UUID id) {
        return meetingJpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public void deleteById(UUID id) {
        try {
            meetingJpaRepository.deleteById(id);
        } catch (EmptyResultDataAccessException ignored) {
            // already gone - deleting a nonexistent meeting is a no-op, not an error
        }
    }

    private boolean violatesConstraint(DataIntegrityViolationException ex, String constraintName) {
        Throwable cause = ex.getCause();
        while (cause != null) {
            if (cause instanceof ConstraintViolationException constraintViolation) {
                String name = constraintViolation.getConstraintName();
                return name != null && name.toLowerCase(Locale.ROOT).contains(constraintName);
            }
            cause = cause.getCause();
        }
        return false;
    }

    private Meeting toDomain(MeetingEntity entity) {
        return new Meeting(entity.getId(), entity.getSlotId(), entity.getTitle(), entity.getDescription(), entity.getParticipants());
    }
}
