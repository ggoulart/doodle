package com.ggoulart.doodle.meeting.application;

import java.util.UUID;

public interface SlotHasMeetingUseCase {

    boolean hasMeeting(UUID slotId);
}
