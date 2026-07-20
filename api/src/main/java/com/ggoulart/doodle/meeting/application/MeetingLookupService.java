package com.ggoulart.doodle.meeting.application;

import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
class MeetingLookupService implements SlotHasMeetingUseCase {

    private final MeetingRepository meetingRepository;

    MeetingLookupService(MeetingRepository meetingRepository) {
        this.meetingRepository = meetingRepository;
    }

    @Override
    public boolean hasMeeting(UUID slotId) {
        return meetingRepository.existsBySlotId(slotId);
    }
}
