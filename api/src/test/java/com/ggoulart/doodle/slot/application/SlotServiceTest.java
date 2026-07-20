package com.ggoulart.doodle.slot.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ggoulart.doodle.calendar.application.GetCalendarUseCase;
import com.ggoulart.doodle.calendar.domain.Calendar;
import com.ggoulart.doodle.meeting.application.MeetingRepository;
import com.ggoulart.doodle.slot.domain.InvalidTimeRangeException;
import com.ggoulart.doodle.slot.domain.Slot;
import com.ggoulart.doodle.slot.domain.SlotStatus;
import com.ggoulart.doodle.user.application.GetUserUseCase;
import com.ggoulart.doodle.user.domain.User;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SlotServiceTest {

    @Mock
    private SlotRepository slotRepository;

    @Mock
    private GetUserUseCase getUserUseCase;

    @Mock
    private GetCalendarUseCase getCalendarUseCase;

    @Mock
    private MeetingRepository meetingRepository;

    @Test
    void createSlotSavesSlotWithCalendarResolvedFromUser() {
        SlotService service = new SlotService(slotRepository, getUserUseCase, getCalendarUseCase, meetingRepository);
        UUID userId = UUID.randomUUID();
        Calendar calendar = new Calendar(UUID.randomUUID(), userId);
        Instant start = Instant.parse("2026-07-20T10:00:00Z");
        Instant end = Instant.parse("2026-07-20T10:30:00Z");
        CreateSlotCommand command = new CreateSlotCommand(userId, start, end, SlotStatus.FREE);

        when(getUserUseCase.getUser(userId)).thenReturn(Optional.of(new User(userId, "Ada Lovelace", "ada@example.com")));
        when(getCalendarUseCase.getCalendarByUserId(userId)).thenReturn(Optional.of(calendar));
        when(slotRepository.save(any(Slot.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Slot slot = service.createSlot(command);

        assertThat(slot.calendarId()).isEqualTo(calendar.id());
        assertThat(slot.startTime()).isEqualTo(start);
        assertThat(slot.endTime()).isEqualTo(end);
        assertThat(slot.status()).isEqualTo(SlotStatus.FREE);
        assertThat(slot.id()).isNotNull();
    }

    @Test
    void createSlotThrowsWhenUserDoesNotExist() {
        SlotService service = new SlotService(slotRepository, getUserUseCase, getCalendarUseCase, meetingRepository);
        UUID userId = UUID.randomUUID();
        CreateSlotCommand command = new CreateSlotCommand(
                userId, Instant.parse("2026-07-20T10:00:00Z"), Instant.parse("2026-07-20T10:30:00Z"), SlotStatus.FREE);
        when(getUserUseCase.getUser(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.createSlot(command)).isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void createSlotThrowsWhenUserHasNoCalendar() {
        SlotService service = new SlotService(slotRepository, getUserUseCase, getCalendarUseCase, meetingRepository);
        UUID userId = UUID.randomUUID();
        CreateSlotCommand command = new CreateSlotCommand(
                userId, Instant.parse("2026-07-20T10:00:00Z"), Instant.parse("2026-07-20T10:30:00Z"), SlotStatus.FREE);
        when(getUserUseCase.getUser(userId)).thenReturn(Optional.of(new User(userId, "Ada Lovelace", "ada@example.com")));
        when(getCalendarUseCase.getCalendarByUserId(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.createSlot(command)).isInstanceOf(CalendarNotFoundException.class);
    }

    @Test
    void deleteSlotDelegatesToRepository() {
        SlotService service = new SlotService(slotRepository, getUserUseCase, getCalendarUseCase, meetingRepository);
        UUID id = UUID.randomUUID();

        service.deleteSlot(id);

        verify(slotRepository).deleteById(id);
    }

    @Test
    void deleteSlotThrowsWhenSlotHasMeeting() {
        SlotService service = new SlotService(slotRepository, getUserUseCase, getCalendarUseCase, meetingRepository);
        UUID id = UUID.randomUUID();
        when(meetingRepository.existsBySlotId(id)).thenReturn(true);

        assertThatThrownBy(() -> service.deleteSlot(id)).isInstanceOf(SlotHasMeetingException.class);

        verify(slotRepository, never()).deleteById(id);
    }

    @Test
    void updateSlotAppliesAllProvidedFields() {
        SlotService service = new SlotService(slotRepository, getUserUseCase, getCalendarUseCase, meetingRepository);
        Slot existing = new Slot(
                UUID.randomUUID(),
                UUID.randomUUID(),
                Instant.parse("2026-07-20T10:00:00Z"),
                Instant.parse("2026-07-20T10:30:00Z"),
                SlotStatus.FREE);
        Instant newStart = Instant.parse("2026-07-20T11:00:00Z");
        Instant newEnd = Instant.parse("2026-07-20T11:30:00Z");
        UpdateSlotCommand command = new UpdateSlotCommand(existing.id(), newStart, newEnd, SlotStatus.BUSY);

        when(slotRepository.findById(existing.id())).thenReturn(Optional.of(existing));
        when(slotRepository.save(any(Slot.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Slot updated = service.updateSlot(command);

        assertThat(updated.id()).isEqualTo(existing.id());
        assertThat(updated.calendarId()).isEqualTo(existing.calendarId());
        assertThat(updated.startTime()).isEqualTo(newStart);
        assertThat(updated.endTime()).isEqualTo(newEnd);
        assertThat(updated.status()).isEqualTo(SlotStatus.BUSY);
    }

    @Test
    void updateSlotKeepsUnprovidedFieldsUnchanged() {
        SlotService service = new SlotService(slotRepository, getUserUseCase, getCalendarUseCase, meetingRepository);
        Slot existing = new Slot(
                UUID.randomUUID(),
                UUID.randomUUID(),
                Instant.parse("2026-07-20T10:00:00Z"),
                Instant.parse("2026-07-20T10:30:00Z"),
                SlotStatus.FREE);
        UpdateSlotCommand command = new UpdateSlotCommand(existing.id(), null, null, SlotStatus.BUSY);

        when(slotRepository.findById(existing.id())).thenReturn(Optional.of(existing));
        when(slotRepository.save(any(Slot.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Slot updated = service.updateSlot(command);

        assertThat(updated.startTime()).isEqualTo(existing.startTime());
        assertThat(updated.endTime()).isEqualTo(existing.endTime());
        assertThat(updated.status()).isEqualTo(SlotStatus.BUSY);
    }

    @Test
    void updateSlotThrowsWhenSlotDoesNotExist() {
        SlotService service = new SlotService(slotRepository, getUserUseCase, getCalendarUseCase, meetingRepository);
        UUID id = UUID.randomUUID();
        UpdateSlotCommand command = new UpdateSlotCommand(id, null, null, SlotStatus.BUSY);
        when(slotRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateSlot(command)).isInstanceOf(SlotNotFoundException.class);
    }

    @Test
    void updateSlotThrowsWhenSlotHasMeeting() {
        SlotService service = new SlotService(slotRepository, getUserUseCase, getCalendarUseCase, meetingRepository);
        Slot existing = new Slot(
                UUID.randomUUID(),
                UUID.randomUUID(),
                Instant.parse("2026-07-20T10:00:00Z"),
                Instant.parse("2026-07-20T10:30:00Z"),
                SlotStatus.BUSY);
        UpdateSlotCommand command = new UpdateSlotCommand(existing.id(), null, null, SlotStatus.FREE);
        when(slotRepository.findById(existing.id())).thenReturn(Optional.of(existing));
        when(meetingRepository.existsBySlotId(existing.id())).thenReturn(true);

        assertThatThrownBy(() -> service.updateSlot(command)).isInstanceOf(SlotHasMeetingException.class);

        verify(slotRepository, never()).save(any());
    }

    @Test
    void updateSlotThrowsWhenResultingTimeRangeIsInvalid() {
        SlotService service = new SlotService(slotRepository, getUserUseCase, getCalendarUseCase, meetingRepository);
        Slot existing = new Slot(
                UUID.randomUUID(),
                UUID.randomUUID(),
                Instant.parse("2026-07-20T10:00:00Z"),
                Instant.parse("2026-07-20T10:30:00Z"),
                SlotStatus.FREE);
        UpdateSlotCommand command = new UpdateSlotCommand(existing.id(), Instant.parse("2026-07-20T11:00:00Z"), null, null);
        when(slotRepository.findById(existing.id())).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> service.updateSlot(command)).isInstanceOf(InvalidTimeRangeException.class);
    }

    @Test
    void querySlotsDelegatesToRepositoryWithResolvedCalendarAndNoStatusFilter() {
        SlotService service = new SlotService(slotRepository, getUserUseCase, getCalendarUseCase, meetingRepository);
        UUID userId = UUID.randomUUID();
        Calendar calendar = new Calendar(UUID.randomUUID(), userId);
        Instant from = Instant.parse("2026-07-20T00:00:00Z");
        Instant to = Instant.parse("2026-07-21T00:00:00Z");
        Slot freeSlot = new Slot(UUID.randomUUID(), calendar.id(), from, from.plusSeconds(1800), SlotStatus.FREE);
        Slot busySlot = new Slot(UUID.randomUUID(), calendar.id(), from.plusSeconds(3600), from.plusSeconds(5400), SlotStatus.BUSY);
        QuerySlotsCommand command = new QuerySlotsCommand(userId, from, to, null);

        when(getUserUseCase.getUser(userId)).thenReturn(Optional.of(new User(userId, "Ada Lovelace", "ada@example.com")));
        when(getCalendarUseCase.getCalendarByUserId(userId)).thenReturn(Optional.of(calendar));
        when(slotRepository.findByCalendarIdAndOverlapping(calendar.id(), from, to, null)).thenReturn(List.of(freeSlot, busySlot));

        List<Slot> slots = service.querySlots(command);

        assertThat(slots).containsExactly(freeSlot, busySlot);
    }

    @Test
    void querySlotsForwardsStatusFilterToRepository() {
        SlotService service = new SlotService(slotRepository, getUserUseCase, getCalendarUseCase, meetingRepository);
        UUID userId = UUID.randomUUID();
        Calendar calendar = new Calendar(UUID.randomUUID(), userId);
        Instant from = Instant.parse("2026-07-20T00:00:00Z");
        Instant to = Instant.parse("2026-07-21T00:00:00Z");
        Slot freeSlot = new Slot(UUID.randomUUID(), calendar.id(), from, from.plusSeconds(1800), SlotStatus.FREE);
        QuerySlotsCommand command = new QuerySlotsCommand(userId, from, to, SlotStatus.FREE);

        when(getUserUseCase.getUser(userId)).thenReturn(Optional.of(new User(userId, "Ada Lovelace", "ada@example.com")));
        when(getCalendarUseCase.getCalendarByUserId(userId)).thenReturn(Optional.of(calendar));
        when(slotRepository.findByCalendarIdAndOverlapping(calendar.id(), from, to, SlotStatus.FREE)).thenReturn(List.of(freeSlot));

        List<Slot> slots = service.querySlots(command);

        assertThat(slots).containsExactly(freeSlot);
    }

    @Test
    void querySlotsThrowsWhenUserDoesNotExist() {
        SlotService service = new SlotService(slotRepository, getUserUseCase, getCalendarUseCase, meetingRepository);
        UUID userId = UUID.randomUUID();
        QuerySlotsCommand command = new QuerySlotsCommand(
                userId, Instant.parse("2026-07-20T00:00:00Z"), Instant.parse("2026-07-21T00:00:00Z"), null);
        when(getUserUseCase.getUser(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.querySlots(command)).isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void querySlotsThrowsWhenUserHasNoCalendar() {
        SlotService service = new SlotService(slotRepository, getUserUseCase, getCalendarUseCase, meetingRepository);
        UUID userId = UUID.randomUUID();
        QuerySlotsCommand command = new QuerySlotsCommand(
                userId, Instant.parse("2026-07-20T00:00:00Z"), Instant.parse("2026-07-21T00:00:00Z"), null);
        when(getUserUseCase.getUser(userId)).thenReturn(Optional.of(new User(userId, "Ada Lovelace", "ada@example.com")));
        when(getCalendarUseCase.getCalendarByUserId(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.querySlots(command)).isInstanceOf(CalendarNotFoundException.class);
    }

    @Test
    void getSlotDelegatesToRepository() {
        SlotService service = new SlotService(slotRepository, getUserUseCase, getCalendarUseCase, meetingRepository);
        Slot slot = new Slot(
                UUID.randomUUID(),
                UUID.randomUUID(),
                Instant.parse("2026-07-20T10:00:00Z"),
                Instant.parse("2026-07-20T10:30:00Z"),
                SlotStatus.FREE);
        when(slotRepository.findById(slot.id())).thenReturn(Optional.of(slot));

        Optional<Slot> result = service.getSlot(slot.id());

        assertThat(result).contains(slot);
    }
}
