package com.ggoulart.doodle.meeting.application;

import com.ggoulart.doodle.meeting.domain.Meeting;

public interface MeetingRepository {

    Meeting save(Meeting meeting);
}
