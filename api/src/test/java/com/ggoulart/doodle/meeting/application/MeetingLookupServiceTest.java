package com.ggoulart.doodle.meeting.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MeetingLookupServiceTest {

    @Mock
    private MeetingRepository meetingRepository;

    @Test
    void hasMeetingDelegatesToRepository() {
        MeetingLookupService service = new MeetingLookupService(meetingRepository);
        UUID slotId = UUID.randomUUID();
        when(meetingRepository.existsBySlotId(slotId)).thenReturn(true);

        assertThat(service.hasMeeting(slotId)).isTrue();
    }

    @Test
    void hasMeetingReturnsFalseWhenNoMeetingExists() {
        MeetingLookupService service = new MeetingLookupService(meetingRepository);
        UUID slotId = UUID.randomUUID();
        when(meetingRepository.existsBySlotId(slotId)).thenReturn(false);

        assertThat(service.hasMeeting(slotId)).isFalse();
    }
}
