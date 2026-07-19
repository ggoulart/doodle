package com.ggoulart.doodle.calendar.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ggoulart.doodle.calendar.application.CalendarAlreadyExistsException;
import com.ggoulart.doodle.calendar.domain.Calendar;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

@ExtendWith(MockitoExtension.class)
class JpaCalendarRepositoryTest {

    @Mock
    private CalendarJpaRepository calendarJpaRepository;

    @Test
    void saveMapsDomainCalendarToEntityAndBackToDomain() {
        JpaCalendarRepository repository = new JpaCalendarRepository(calendarJpaRepository);
        Calendar calendar = new Calendar(UUID.randomUUID(), UUID.randomUUID());
        when(calendarJpaRepository.saveAndFlush(any(CalendarEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Calendar saved = repository.save(calendar);

        assertThat(saved).isEqualTo(calendar);
        ArgumentCaptor<CalendarEntity> captor = ArgumentCaptor.forClass(CalendarEntity.class);
        verify(calendarJpaRepository).saveAndFlush(captor.capture());
        assertThat(captor.getValue().getId()).isEqualTo(calendar.id());
        assertThat(captor.getValue().getUserId()).isEqualTo(calendar.userId());
    }

    @Test
    void saveThrowsCalendarAlreadyExistsExceptionWhenUserIdConstraintViolated() {
        JpaCalendarRepository repository = new JpaCalendarRepository(calendarJpaRepository);
        Calendar calendar = new Calendar(UUID.randomUUID(), UUID.randomUUID());
        when(calendarJpaRepository.saveAndFlush(any(CalendarEntity.class)))
                .thenThrow(wrapConstraintViolation("uk_calendars_user_id"));

        assertThatThrownBy(() -> repository.save(calendar)).isInstanceOf(CalendarAlreadyExistsException.class);
    }

    @Test
    void saveRethrowsWhenViolatedConstraintIsUnrelated() {
        JpaCalendarRepository repository = new JpaCalendarRepository(calendarJpaRepository);
        Calendar calendar = new Calendar(UUID.randomUUID(), UUID.randomUUID());
        DataIntegrityViolationException exception = wrapConstraintViolation("some_other_constraint");
        when(calendarJpaRepository.saveAndFlush(any(CalendarEntity.class))).thenThrow(exception);

        assertThatThrownBy(() -> repository.save(calendar)).isSameAs(exception);
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

    private DataIntegrityViolationException wrapConstraintViolation(String constraintName) {
        ConstraintViolationException constraintViolationException = new ConstraintViolationException(
                "could not execute statement", new SQLException("constraint violated"), constraintName);
        return new DataIntegrityViolationException("could not execute statement", constraintViolationException);
    }
}
