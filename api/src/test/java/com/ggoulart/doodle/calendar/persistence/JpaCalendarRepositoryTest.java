package com.ggoulart.doodle.calendar.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ggoulart.doodle.calendar.domain.Calendar;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class JpaCalendarRepositoryTest {

    @Mock
    private CalendarJpaRepository calendarJpaRepository;

    @Test
    void saveMapsDomainCalendarToEntityAndBackToDomain() {
        JpaCalendarRepository repository = new JpaCalendarRepository(calendarJpaRepository);
        Calendar calendar = new Calendar(UUID.randomUUID(), UUID.randomUUID());
        when(calendarJpaRepository.save(any(CalendarEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Calendar saved = repository.save(calendar);

        assertThat(saved).isEqualTo(calendar);
        ArgumentCaptor<CalendarEntity> captor = ArgumentCaptor.forClass(CalendarEntity.class);
        verify(calendarJpaRepository).save(captor.capture());
        assertThat(captor.getValue().getId()).isEqualTo(calendar.id());
        assertThat(captor.getValue().getUserId()).isEqualTo(calendar.userId());
    }

    @Test
    void findByUserIdMapsEntityToDomainWhenPresent() {
        JpaCalendarRepository repository = new JpaCalendarRepository(calendarJpaRepository);
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        CalendarEntity entity = new CalendarEntity(id, userId);
        when(calendarJpaRepository.findByUserId(userId)).thenReturn(Optional.of(entity));

        Optional<Calendar> found = repository.findByUserId(userId);

        assertThat(found).contains(new Calendar(id, userId));
    }

    @Test
    void findByUserIdReturnsEmptyWhenMissing() {
        JpaCalendarRepository repository = new JpaCalendarRepository(calendarJpaRepository);
        UUID userId = UUID.randomUUID();
        when(calendarJpaRepository.findByUserId(userId)).thenReturn(Optional.empty());

        Optional<Calendar> found = repository.findByUserId(userId);

        assertThat(found).isEmpty();
    }
}
