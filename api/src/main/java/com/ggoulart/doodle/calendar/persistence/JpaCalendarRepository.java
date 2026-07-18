package com.ggoulart.doodle.calendar.persistence;

import com.ggoulart.doodle.calendar.application.CalendarRepository;
import com.ggoulart.doodle.calendar.domain.Calendar;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
class JpaCalendarRepository implements CalendarRepository {

    private final CalendarJpaRepository calendarJpaRepository;

    JpaCalendarRepository(CalendarJpaRepository calendarJpaRepository) {
        this.calendarJpaRepository = calendarJpaRepository;
    }

    @Override
    public Calendar save(Calendar calendar) {
        CalendarEntity entity = new CalendarEntity(calendar.id(), calendar.userId());
        CalendarEntity saved = calendarJpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<Calendar> findByUserId(UUID userId) {
        return calendarJpaRepository.findByUserId(userId).map(this::toDomain);
    }

    private Calendar toDomain(CalendarEntity entity) {
        return new Calendar(entity.getId(), entity.getUserId());
    }
}
