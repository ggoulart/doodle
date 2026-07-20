package com.ggoulart.doodle.meeting.application;

import com.ggoulart.doodle.meeting.domain.Meeting;
import java.util.Optional;
import java.util.UUID;

public interface MeetingRepository {

    Meeting save(Meeting meeting);

    Optional<Meeting> findById(UUID id);

    boolean existsBySlotId(UUID slotId);

    void deleteById(UUID id);
}
