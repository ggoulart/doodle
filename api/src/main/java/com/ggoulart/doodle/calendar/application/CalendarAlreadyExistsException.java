package com.ggoulart.doodle.calendar.application;

import java.util.UUID;

public class CalendarAlreadyExistsException extends RuntimeException {

    public CalendarAlreadyExistsException(UUID userId) {
        super("Calendar already exists for user: " + userId);
    }
}
