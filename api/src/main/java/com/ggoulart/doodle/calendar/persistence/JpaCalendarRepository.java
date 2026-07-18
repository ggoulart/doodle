package com.ggoulart.doodle.calendar.persistence;

import com.ggoulart.doodle.calendar.application.CalendarRepository;
import com.ggoulart.doodle.calendar.domain.Calendar;
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
        return new Calendar(saved.getId(), saved.getUserId());
    }
}
