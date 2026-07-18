package com.ggoulart.doodle.slot.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ggoulart.doodle.calendar.application.GetCalendarUseCase;
import com.ggoulart.doodle.calendar.domain.Calendar;
import com.ggoulart.doodle.slot.domain.Slot;
import com.ggoulart.doodle.slot.domain.SlotStatus;
import com.ggoulart.doodle.user.application.GetUserUseCase;
import com.ggoulart.doodle.user.domain.User;
import java.time.Instant;
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

    @Test
    void createSlotSavesSlotWithCalendarResolvedFromUser() {
        SlotService service = new SlotService(slotRepository, getUserUseCase, getCalendarUseCase);
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
    void createSlotThrowsWhenEndTimeNotAfterStartTime() {
        SlotService service = new SlotService(slotRepository, getUserUseCase, getCalendarUseCase);
        Instant start = Instant.parse("2026-07-20T10:00:00Z");
        CreateSlotCommand command = new CreateSlotCommand(UUID.randomUUID(), start, start, SlotStatus.FREE);

        assertThatThrownBy(() -> service.createSlot(command)).isInstanceOf(InvalidTimeRangeException.class);
    }

    @Test
    void createSlotThrowsWhenUserDoesNotExist() {
        SlotService service = new SlotService(slotRepository, getUserUseCase, getCalendarUseCase);
        UUID userId = UUID.randomUUID();
        CreateSlotCommand command = new CreateSlotCommand(
                userId, Instant.parse("2026-07-20T10:00:00Z"), Instant.parse("2026-07-20T10:30:00Z"), SlotStatus.FREE);
        when(getUserUseCase.getUser(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.createSlot(command)).isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void createSlotThrowsWhenUserHasNoCalendar() {
        SlotService service = new SlotService(slotRepository, getUserUseCase, getCalendarUseCase);
        UUID userId = UUID.randomUUID();
        CreateSlotCommand command = new CreateSlotCommand(
                userId, Instant.parse("2026-07-20T10:00:00Z"), Instant.parse("2026-07-20T10:30:00Z"), SlotStatus.FREE);
        when(getUserUseCase.getUser(userId)).thenReturn(Optional.of(new User(userId, "Ada Lovelace", "ada@example.com")));
        when(getCalendarUseCase.getCalendarByUserId(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.createSlot(command)).isInstanceOf(CalendarNotFoundException.class);
    }

    @Test
    void deleteSlotDelegatesToRepository() {
        SlotService service = new SlotService(slotRepository, getUserUseCase, getCalendarUseCase);
        UUID id = UUID.randomUUID();

        service.deleteSlot(id);

        verify(slotRepository).deleteById(id);
    }

    @Test
    void updateSlotAppliesAllProvidedFields() {
        SlotService service = new SlotService(slotRepository, getUserUseCase, getCalendarUseCase);
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
        SlotService service = new SlotService(slotRepository, getUserUseCase, getCalendarUseCase);
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
        SlotService service = new SlotService(slotRepository, getUserUseCase, getCalendarUseCase);
        UUID id = UUID.randomUUID();
        UpdateSlotCommand command = new UpdateSlotCommand(id, null, null, SlotStatus.BUSY);
        when(slotRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateSlot(command)).isInstanceOf(SlotNotFoundException.class);
    }

    @Test
    void updateSlotThrowsWhenResultingTimeRangeIsInvalid() {
        SlotService service = new SlotService(slotRepository, getUserUseCase, getCalendarUseCase);
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
}
