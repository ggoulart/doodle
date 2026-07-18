package com.ggoulart.doodle.slot.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ggoulart.doodle.slot.domain.Slot;
import com.ggoulart.doodle.slot.domain.SlotStatus;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.EmptyResultDataAccessException;

@ExtendWith(MockitoExtension.class)
class JpaSlotRepositoryTest {

    @Mock
    private SlotJpaRepository slotJpaRepository;

    @Test
    void saveMapsDomainSlotToEntityAndBackToDomain() {
        JpaSlotRepository repository = new JpaSlotRepository(slotJpaRepository);
        Slot slot = new Slot(
                UUID.randomUUID(),
                UUID.randomUUID(),
                Instant.parse("2026-07-20T10:00:00Z"),
                Instant.parse("2026-07-20T10:30:00Z"),
                SlotStatus.FREE);
        when(slotJpaRepository.save(any(SlotEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Slot saved = repository.save(slot);

        assertThat(saved).isEqualTo(slot);
        ArgumentCaptor<SlotEntity> captor = ArgumentCaptor.forClass(SlotEntity.class);
        verify(slotJpaRepository).save(captor.capture());
        assertThat(captor.getValue().getId()).isEqualTo(slot.id());
        assertThat(captor.getValue().getCalendarId()).isEqualTo(slot.calendarId());
        assertThat(captor.getValue().getStartTime()).isEqualTo(slot.startTime());
        assertThat(captor.getValue().getEndTime()).isEqualTo(slot.endTime());
        assertThat(captor.getValue().getStatus()).isEqualTo(slot.status());
    }

    @Test
    void findByIdMapsEntityToDomainWhenPresent() {
        JpaSlotRepository repository = new JpaSlotRepository(slotJpaRepository);
        UUID id = UUID.randomUUID();
        UUID calendarId = UUID.randomUUID();
        Instant start = Instant.parse("2026-07-20T10:00:00Z");
        Instant end = Instant.parse("2026-07-20T10:30:00Z");
        SlotEntity entity = new SlotEntity(id, calendarId, start, end, SlotStatus.FREE);
        when(slotJpaRepository.findById(id)).thenReturn(Optional.of(entity));

        Optional<Slot> found = repository.findById(id);

        assertThat(found).contains(new Slot(id, calendarId, start, end, SlotStatus.FREE));
    }

    @Test
    void findByIdReturnsEmptyWhenMissing() {
        JpaSlotRepository repository = new JpaSlotRepository(slotJpaRepository);
        UUID id = UUID.randomUUID();
        when(slotJpaRepository.findById(id)).thenReturn(Optional.empty());

        Optional<Slot> found = repository.findById(id);

        assertThat(found).isEmpty();
    }

    @Test
    void deleteByIdDelegatesToJpaRepository() {
        JpaSlotRepository repository = new JpaSlotRepository(slotJpaRepository);
        UUID id = UUID.randomUUID();

        repository.deleteById(id);

        verify(slotJpaRepository).deleteById(id);
    }

    @Test
    void deleteByIdIsNoOpWhenSlotDoesNotExist() {
        JpaSlotRepository repository = new JpaSlotRepository(slotJpaRepository);
        UUID id = UUID.randomUUID();
        doThrow(new EmptyResultDataAccessException(1)).when(slotJpaRepository).deleteById(id);

        assertThatCode(() -> repository.deleteById(id)).doesNotThrowAnyException();
    }
}
