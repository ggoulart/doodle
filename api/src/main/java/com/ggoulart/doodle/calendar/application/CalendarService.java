package com.ggoulart.doodle.calendar.application;

import com.ggoulart.doodle.calendar.domain.Calendar;

import java.util.UUID;

import org.springframework.stereotype.Service;

@Service
class CalendarService implements CreateCalendarUseCase {

    private final CalendarRepository calendarRepository;

    CalendarService(CalendarRepository calendarRepository) {
        this.calendarRepository = calendarRepository;
    }

    @Override
    public void createCalendar(UUID userId) {
        Calendar calendar = new Calendar(UUID.randomUUID(), userId);
        calendarRepository.save(calendar);
    }
}
