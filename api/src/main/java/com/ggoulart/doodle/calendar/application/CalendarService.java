package com.ggoulart.doodle.calendar.application;

import com.ggoulart.doodle.calendar.domain.Calendar;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
class CalendarService implements CreateCalendarUseCase, GetCalendarUseCase {

    private final CalendarRepository calendarRepository;

    CalendarService(CalendarRepository calendarRepository) {
        this.calendarRepository = calendarRepository;
    }

    @Override
    public void createCalendar(UUID userId) {
        Calendar calendar = new Calendar(UUID.randomUUID(), userId);
        calendarRepository.save(calendar);
    }

    @Override
    public Optional<Calendar> getCalendarByUserId(UUID userId) {
        return calendarRepository.findByUserId(userId);
    }
}
