package com.ggoulart.doodle.meeting.application;

import java.util.UUID;

public class SlotNotFreeException extends RuntimeException {

    public SlotNotFreeException(UUID slotId) {
        super("Slot is not free: " + slotId);
    }
}
