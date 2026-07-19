package com.ggoulart.doodle.calendar.persistence;

import com.ggoulart.doodle.calendar.application.CalendarAlreadyExistsException;
import com.ggoulart.doodle.calendar.application.CalendarRepository;
import com.ggoulart.doodle.calendar.domain.Calendar;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;

@Repository
class JpaCalendarRepository implements CalendarRepository {

    private static final String USER_ID_UNIQUE_CONSTRAINT = "uk_calendars_user_id";

    private final CalendarJpaRepository calendarJpaRepository;

    JpaCalendarRepository(CalendarJpaRepository calendarJpaRepository) {
        this.calendarJpaRepository = calendarJpaRepository;
    }

    @Override
    public Calendar save(Calendar calendar) {
        CalendarEntity entity = new CalendarEntity(calendar.id(), calendar.userId());
        try {
            CalendarEntity saved = calendarJpaRepository.saveAndFlush(entity);
            return toDomain(saved);
        } catch (DataIntegrityViolationException ex) {
            if (violatesConstraint(ex, USER_ID_UNIQUE_CONSTRAINT)) {
                throw new CalendarAlreadyExistsException(calendar.userId());
            }
            throw ex;
        }
    }

    @Override
    public Optional<Calendar> findByUserId(UUID userId) {
        return calendarJpaRepository.findByUserId(userId).map(this::toDomain);
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

    private Calendar toDomain(CalendarEntity entity) {
        return new Calendar(entity.getId(), entity.getUserId());
    }
}
