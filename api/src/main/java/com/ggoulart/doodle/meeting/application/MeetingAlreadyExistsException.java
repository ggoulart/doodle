package com.ggoulart.doodle.meeting.application;

import java.util.UUID;

public class MeetingAlreadyExistsException extends RuntimeException {

    public MeetingAlreadyExistsException(UUID slotId) {
        super("Slot already has a meeting: " + slotId);
    }
}
