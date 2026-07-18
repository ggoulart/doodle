package com.ggoulart.doodle.slot.application;

import com.ggoulart.doodle.calendar.application.GetCalendarUseCase;
import com.ggoulart.doodle.calendar.domain.Calendar;
import com.ggoulart.doodle.slot.domain.Slot;
import com.ggoulart.doodle.user.application.GetUserUseCase;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
class SlotService implements CreateSlotUseCase, DeleteSlotUseCase {

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
        if (!command.endTime().isAfter(command.startTime())) {
            throw new InvalidTimeRangeException("endTime must be after startTime");
        }

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
}
