package com.ggoulart.doodle.meeting.application;

import com.ggoulart.doodle.meeting.domain.Meeting;
import java.util.Optional;
import java.util.UUID;

public interface GetMeetingUseCase {

    Optional<Meeting> getMeeting(UUID id);
}
