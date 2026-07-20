package com.ggoulart.doodle.meeting.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ggoulart.doodle.meeting.domain.Meeting;
import com.ggoulart.doodle.slot.application.GetSlotUseCase;
import com.ggoulart.doodle.slot.application.UpdateSlotCommand;
import com.ggoulart.doodle.slot.application.UpdateSlotUseCase;
import com.ggoulart.doodle.slot.domain.Slot;
import com.ggoulart.doodle.slot.domain.SlotStatus;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MeetingServiceTest {

    @Mock
    private MeetingRepository meetingRepository;

    @Mock
    private GetSlotUseCase getSlotUseCase;

    @Mock
    private UpdateSlotUseCase updateSlotUseCase;

    @Test
    void bookSlotCreatesMeetingAndMarksSlotBusy() {
        MeetingService service = new MeetingService(meetingRepository, getSlotUseCase, updateSlotUseCase);
        Slot slot = new Slot(
                UUID.randomUUID(),
                UUID.randomUUID(),
                Instant.parse("2026-07-20T10:00:00Z"),
                Instant.parse("2026-07-20T10:30:00Z"),
                SlotStatus.FREE);
        Slot busySlot = new Slot(slot.id(), slot.calendarId(), slot.startTime(), slot.endTime(), SlotStatus.BUSY);
        BookSlotCommand command = new BookSlotCommand(
                slot.id(), "Planning", "Sprint planning", List.of("ada@example.com", "grace@example.com"));

        when(getSlotUseCase.getSlot(slot.id())).thenReturn(Optional.of(slot));
        when(meetingRepository.save(any(Meeting.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(updateSlotUseCase.updateSlot(new UpdateSlotCommand(slot.id(), null, null, SlotStatus.BUSY))).thenReturn(busySlot);

        BookSlotResult result = service.bookSlot(command);

        assertThat(result.meeting().slotId()).isEqualTo(slot.id());
        assertThat(result.meeting().title()).isEqualTo("Planning");
        assertThat(result.meeting().description()).isEqualTo("Sprint planning");
        assertThat(result.meeting().participants()).containsExactly("ada@example.com", "grace@example.com");
        assertThat(result.slot()).isEqualTo(busySlot);
    }

    @Test
    void bookSlotThrowsWhenSlotDoesNotExist() {
        MeetingService service = new MeetingService(meetingRepository, getSlotUseCase, updateSlotUseCase);
        UUID slotId = UUID.randomUUID();
        BookSlotCommand command = new BookSlotCommand(slotId, "Planning", null, List.of());
        when(getSlotUseCase.getSlot(slotId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.bookSlot(command)).isInstanceOf(SlotNotFoundException.class);
    }

    @Test
    void bookSlotThrowsWhenSlotIsNotFree() {
        MeetingService service = new MeetingService(meetingRepository, getSlotUseCase, updateSlotUseCase);
        Slot slot = new Slot(
                UUID.randomUUID(),
                UUID.randomUUID(),
                Instant.parse("2026-07-20T10:00:00Z"),
                Instant.parse("2026-07-20T10:30:00Z"),
                SlotStatus.BUSY);
        BookSlotCommand command = new BookSlotCommand(slot.id(), "Planning", null, List.of());
        when(getSlotUseCase.getSlot(slot.id())).thenReturn(Optional.of(slot));

        assertThatThrownBy(() -> service.bookSlot(command)).isInstanceOf(SlotNotFreeException.class);
    }

    @Test
    void bookSlotDefaultsTitleWhenBlank() {
        MeetingService service = new MeetingService(meetingRepository, getSlotUseCase, updateSlotUseCase);
        Slot slot = new Slot(
                UUID.randomUUID(),
                UUID.randomUUID(),
                Instant.parse("2026-07-20T10:00:00Z"),
                Instant.parse("2026-07-20T10:30:00Z"),
                SlotStatus.FREE);
        Slot busySlot = new Slot(slot.id(), slot.calendarId(), slot.startTime(), slot.endTime(), SlotStatus.BUSY);
        BookSlotCommand command = new BookSlotCommand(slot.id(), "   ", null, List.of());

        when(getSlotUseCase.getSlot(slot.id())).thenReturn(Optional.of(slot));
        when(meetingRepository.save(any(Meeting.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(updateSlotUseCase.updateSlot(new UpdateSlotCommand(slot.id(), null, null, SlotStatus.BUSY))).thenReturn(busySlot);

        BookSlotResult result = service.bookSlot(command);

        assertThat(result.meeting().title()).isEqualTo("(No title)");
    }

    @Test
    void deleteMeetingDeletesMeetingAndFreesSlot() {
        MeetingService service = new MeetingService(meetingRepository, getSlotUseCase, updateSlotUseCase);
        Meeting meeting = new Meeting(UUID.randomUUID(), UUID.randomUUID(), "Planning", null, List.of());
        when(meetingRepository.findById(meeting.id())).thenReturn(Optional.of(meeting));

        service.deleteMeeting(meeting.id());

        verify(meetingRepository).deleteById(meeting.id());
        verify(updateSlotUseCase).updateSlot(new UpdateSlotCommand(meeting.slotId(), null, null, SlotStatus.FREE));
    }

    @Test
    void deleteMeetingIsNoOpWhenMeetingDoesNotExist() {
        MeetingService service = new MeetingService(meetingRepository, getSlotUseCase, updateSlotUseCase);
        UUID id = UUID.randomUUID();
        when(meetingRepository.findById(id)).thenReturn(Optional.empty());

        service.deleteMeeting(id);

        verify(meetingRepository, never()).deleteById(any());
        verify(updateSlotUseCase, never()).updateSlot(any());
    }

    @Test
    void getMeetingReturnsMeetingWhenFound() {
        MeetingService service = new MeetingService(meetingRepository, getSlotUseCase, updateSlotUseCase);
        Meeting meeting = new Meeting(UUID.randomUUID(), UUID.randomUUID(), "Planning", null, List.of());
        when(meetingRepository.findById(meeting.id())).thenReturn(Optional.of(meeting));

        Optional<Meeting> result = service.getMeeting(meeting.id());

        assertThat(result).contains(meeting);
    }

    @Test
    void getMeetingReturnsEmptyWhenMissing() {
        MeetingService service = new MeetingService(meetingRepository, getSlotUseCase, updateSlotUseCase);
        UUID id = UUID.randomUUID();
        when(meetingRepository.findById(id)).thenReturn(Optional.empty());

        Optional<Meeting> result = service.getMeeting(id);

        assertThat(result).isEmpty();
    }
}
