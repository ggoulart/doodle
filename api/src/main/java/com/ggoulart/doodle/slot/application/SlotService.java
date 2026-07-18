package com.ggoulart.doodle.slot.application;

import com.ggoulart.doodle.calendar.application.GetCalendarUseCase;
import com.ggoulart.doodle.calendar.domain.Calendar;
import com.ggoulart.doodle.slot.domain.Slot;
import com.ggoulart.doodle.slot.domain.SlotStatus;
import com.ggoulart.doodle.user.application.GetUserUseCase;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
class SlotService implements CreateSlotUseCase, DeleteSlotUseCase, UpdateSlotUseCase, QuerySlotsUseCase, GetSlotUseCase {

    private final SlotRepository slotRepository;
    private final GetUserUseCase getUserUseCase;
    private final GetCalendarUseCase getCalendarUseCase;

    SlotService(SlotRepository slotRepository, GetUserUseCase getUserUseCase, GetCalendarUseCase getCalendarUseCase) {
        this.slotRepository = slotRepository;
        this.getUserUseCase = getUserUseCase;
        this.getCalendarUseCase = getCalendarUseCase;
    }

    @Override
    public Slot createSlot(CreateSlotCommand command) {
        UUID userId = command.userId();
        if (getUserUseCase.getUser(userId).isEmpty()) {
            throw new UserNotFoundException(userId);
        }

        Calendar calendar = getCalendarUseCase.getCalendarByUserId(userId)
                .orElseThrow(() -> new CalendarNotFoundException(userId));

        Slot slot = new Slot(UUID.randomUUID(), calendar.id(), command.startTime(), command.endTime(), command.status());
        return slotRepository.save(slot);
    }

    @Override
    public void deleteSlot(UUID id) {
        slotRepository.deleteById(id);
    }

    @Override
    public Slot updateSlot(UpdateSlotCommand command) {
        Slot existing = slotRepository.findById(command.id())
                .orElseThrow(() -> new SlotNotFoundException(command.id()));

        Instant startTime = command.startTime() != null ? command.startTime() : existing.startTime();
        Instant endTime = command.endTime() != null ? command.endTime() : existing.endTime();
        SlotStatus status = command.status() != null ? command.status() : existing.status();

        Slot updated = new Slot(existing.id(), existing.calendarId(), startTime, endTime, status);
        return slotRepository.save(updated);
    }

    @Override
    public List<Slot> querySlots(QuerySlotsCommand command) {
        UUID userId = command.userId();
        if (getUserUseCase.getUser(userId).isEmpty()) {
            throw new UserNotFoundException(userId);
        }

        Calendar calendar = getCalendarUseCase.getCalendarByUserId(userId)
                .orElseThrow(() -> new CalendarNotFoundException(userId));

        return slotRepository.findByCalendarIdAndOverlapping(calendar.id(), command.from(), command.to(), command.status());
    }

    @Override
    public Optional<Slot> getSlot(UUID id) {
        return slotRepository.findById(id);
    }
}
