package com.ggoulart.doodle.meeting.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ggoulart.doodle.meeting.domain.Meeting;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class JpaMeetingRepositoryTest {

    @Mock
    private MeetingJpaRepository meetingJpaRepository;

    @Test
    void saveMapsDomainMeetingToEntityAndBackToDomain() {
        JpaMeetingRepository repository = new JpaMeetingRepository(meetingJpaRepository);
        Meeting meeting = new Meeting(
                UUID.randomUUID(), UUID.randomUUID(), "Planning", "Sprint planning", List.of("ada@example.com"));
        when(meetingJpaRepository.save(any(MeetingEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Meeting saved = repository.save(meeting);

        assertThat(saved).isEqualTo(meeting);
        ArgumentCaptor<MeetingEntity> captor = ArgumentCaptor.forClass(MeetingEntity.class);
        verify(meetingJpaRepository).save(captor.capture());
        assertThat(captor.getValue().getId()).isEqualTo(meeting.id());
        assertThat(captor.getValue().getSlotId()).isEqualTo(meeting.slotId());
        assertThat(captor.getValue().getTitle()).isEqualTo(meeting.title());
        assertThat(captor.getValue().getDescription()).isEqualTo(meeting.description());
        assertThat(captor.getValue().getParticipants()).isEqualTo(meeting.participants());
    }

    @Test
    void existsBySlotIdDelegatesToJpaRepository() {
        JpaMeetingRepository repository = new JpaMeetingRepository(meetingJpaRepository);
        UUID slotId = UUID.randomUUID();
        when(meetingJpaRepository.existsBySlotId(slotId)).thenReturn(true);

        boolean exists = repository.existsBySlotId(slotId);

        assertThat(exists).isTrue();
    }
}
