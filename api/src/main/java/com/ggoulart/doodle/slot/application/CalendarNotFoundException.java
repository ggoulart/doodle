package com.ggoulart.doodle.slot.application;

import java.util.UUID;

public class CalendarNotFoundException extends RuntimeException {

    public CalendarNotFoundException(UUID userId) {
        super("Calendar not found for user: " + userId);
    }
}
