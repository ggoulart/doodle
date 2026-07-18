package com.ggoulart.doodle.slot.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
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
}
