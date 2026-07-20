package com.ggoulart.doodle.meeting.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ggoulart.doodle.meeting.application.MeetingAlreadyExistsException;
import com.ggoulart.doodle.meeting.domain.InvalidParticipantException;
import com.ggoulart.doodle.meeting.domain.Meeting;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;

@ExtendWith(MockitoExtension.class)
class JpaMeetingRepositoryTest {

    @Mock
    private MeetingJpaRepository meetingJpaRepository;

    @Test
    void saveMapsDomainMeetingToEntityAndBackToDomain() {
        JpaMeetingRepository repository = new JpaMeetingRepository(meetingJpaRepository);
        Meeting meeting = new Meeting(
                UUID.randomUUID(), UUID.randomUUID(), "Planning", "Sprint planning", List.of("ada@example.com"));
        when(meetingJpaRepository.saveAndFlush(any(MeetingEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Meeting saved = repository.save(meeting);

        assertThat(saved).isEqualTo(meeting);
        ArgumentCaptor<MeetingEntity> captor = ArgumentCaptor.forClass(MeetingEntity.class);
        verify(meetingJpaRepository).saveAndFlush(captor.capture());
        assertThat(captor.getValue().getId()).isEqualTo(meeting.id());
        assertThat(captor.getValue().getSlotId()).isEqualTo(meeting.slotId());
        assertThat(captor.getValue().getTitle()).isEqualTo(meeting.title());
        assertThat(captor.getValue().getDescription()).isEqualTo(meeting.description());
        assertThat(captor.getValue().getParticipants()).isEqualTo(meeting.participants());
    }

    @Test
    void saveThrowsMeetingAlreadyExistsExceptionWhenSlotIdConstraintViolated() {
        JpaMeetingRepository repository = new JpaMeetingRepository(meetingJpaRepository);
        Meeting meeting = new Meeting(
                UUID.randomUUID(), UUID.randomUUID(), "Planning", null, List.of());
        when(meetingJpaRepository.saveAndFlush(any(MeetingEntity.class)))
                .thenThrow(wrapConstraintViolation("uk_meetings_slot_id"));

        assertThatThrownBy(() -> repository.save(meeting)).isInstanceOf(MeetingAlreadyExistsException.class);
    }

    @Test
    void saveThrowsInvalidParticipantExceptionWhenParticipantsConstraintViolated() {
        JpaMeetingRepository repository = new JpaMeetingRepository(meetingJpaRepository);
        Meeting meeting = new Meeting(
                UUID.randomUUID(), UUID.randomUUID(), "Planning", null, List.of("ada@example.com"));
        when(meetingJpaRepository.saveAndFlush(any(MeetingEntity.class)))
                .thenThrow(wrapConstraintViolation("uk_meeting_participants"));

        assertThatThrownBy(() -> repository.save(meeting)).isInstanceOf(InvalidParticipantException.class);
    }

    @Test
    void saveRethrowsWhenViolatedConstraintIsUnrelated() {
        JpaMeetingRepository repository = new JpaMeetingRepository(meetingJpaRepository);
        Meeting meeting = new Meeting(
                UUID.randomUUID(), UUID.randomUUID(), "Planning", null, List.of());
        DataIntegrityViolationException exception = wrapConstraintViolation("some_other_constraint");
        when(meetingJpaRepository.saveAndFlush(any(MeetingEntity.class))).thenThrow(exception);

        assertThatThrownBy(() -> repository.save(meeting)).isSameAs(exception);
    }

    @Test
    void findByIdMapsEntityToDomainWhenPresent() {
        JpaMeetingRepository repository = new JpaMeetingRepository(meetingJpaRepository);
        UUID id = UUID.randomUUID();
        UUID slotId = UUID.randomUUID();
        MeetingEntity entity = new MeetingEntity(id, slotId, "Planning", null, List.of());
        when(meetingJpaRepository.findById(id)).thenReturn(Optional.of(entity));

        Optional<Meeting> found = repository.findById(id);

        assertThat(found).contains(new Meeting(id, slotId, "Planning", null, List.of()));
    }

    @Test
    void findByIdReturnsEmptyWhenMissing() {
        JpaMeetingRepository repository = new JpaMeetingRepository(meetingJpaRepository);
        UUID id = UUID.randomUUID();
        when(meetingJpaRepository.findById(id)).thenReturn(Optional.empty());

        Optional<Meeting> found = repository.findById(id);

        assertThat(found).isEmpty();
    }

    @Test
    void deleteByIdDelegatesToJpaRepository() {
        JpaMeetingRepository repository = new JpaMeetingRepository(meetingJpaRepository);
        UUID id = UUID.randomUUID();

        repository.deleteById(id);

        verify(meetingJpaRepository).deleteById(id);
    }

    @Test
    void deleteByIdIsNoOpWhenMeetingDoesNotExist() {
        JpaMeetingRepository repository = new JpaMeetingRepository(meetingJpaRepository);
        UUID id = UUID.randomUUID();
        doThrow(new EmptyResultDataAccessException(1)).when(meetingJpaRepository).deleteById(id);

        assertThatCode(() -> repository.deleteById(id)).doesNotThrowAnyException();
    }

    private DataIntegrityViolationException wrapConstraintViolation(String constraintName) {
        ConstraintViolationException constraintViolationException = new ConstraintViolationException(
                "could not execute statement", new SQLException("constraint violated"), constraintName);
        return new DataIntegrityViolationException("could not execute statement", constraintViolationException);
    }
}
