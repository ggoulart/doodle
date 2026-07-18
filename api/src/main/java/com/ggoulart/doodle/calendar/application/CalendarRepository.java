package com.ggoulart.doodle.calendar.application;

import com.ggoulart.doodle.calendar.domain.Calendar;

public interface CalendarRepository {

    Calendar save(Calendar calendar);
}
