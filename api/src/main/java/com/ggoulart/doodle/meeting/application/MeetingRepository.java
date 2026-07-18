package com.ggoulart.doodle.meeting.application;

import com.ggoulart.doodle.meeting.domain.Meeting;
import java.util.UUID;

public interface MeetingRepository {

    Meeting save(Meeting meeting);

    boolean existsBySlotId(UUID slotId);
}
