package com.ggoulart.doodle.slot.application;

import java.util.UUID;

public class SlotHasMeetingException extends RuntimeException {

    public SlotHasMeetingException(UUID slotId) {
        super("Slot has a meeting and cannot be deleted; delete the meeting first: " + slotId);
    }
}
