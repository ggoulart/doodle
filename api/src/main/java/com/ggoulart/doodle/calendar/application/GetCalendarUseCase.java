package com.ggoulart.doodle.calendar.application;

import com.ggoulart.doodle.calendar.domain.Calendar;
import java.util.Optional;
import java.util.UUID;

public interface GetCalendarUseCase {

    Optional<Calendar> getCalendarByUserId(UUID userId);
}
