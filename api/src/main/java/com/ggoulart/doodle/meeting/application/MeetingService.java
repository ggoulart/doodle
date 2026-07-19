package com.ggoulart.doodle.meeting.application;

import com.ggoulart.doodle.meeting.domain.Meeting;
import com.ggoulart.doodle.slot.application.GetSlotUseCase;
import com.ggoulart.doodle.slot.application.UpdateSlotCommand;
import com.ggoulart.doodle.slot.application.UpdateSlotUseCase;
import com.ggoulart.doodle.slot.domain.Slot;
import com.ggoulart.doodle.slot.domain.SlotStatus;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class MeetingService implements BookSlotUseCase {

    private final MeetingRepository meetingRepository;
    private final GetSlotUseCase getSlotUseCase;
    private final UpdateSlotUseCase updateSlotUseCase;

    MeetingService(MeetingRepository meetingRepository, GetSlotUseCase getSlotUseCase, UpdateSlotUseCase updateSlotUseCase) {
        this.meetingRepository = meetingRepository;
        this.getSlotUseCase = getSlotUseCase;
        this.updateSlotUseCase = updateSlotUseCase;
    }

    @Transactional
    @Override
    public BookSlotResult bookSlot(BookSlotCommand command) {
        UUID slotId = command.slotId();
        Slot slot = getSlotUseCase.getSlot(slotId).orElseThrow(() -> new SlotNotFoundException(slotId));

        if (slot.status() != SlotStatus.FREE) {
            throw new SlotNotFreeException(slotId);
        }

        Meeting meeting = new Meeting(
                UUID.randomUUID(), slotId, command.title(), command.description(), command.participants());
        Meeting savedMeeting = meetingRepository.save(meeting);

        Slot updatedSlot = updateSlotUseCase.updateSlot(new UpdateSlotCommand(slotId, null, null, SlotStatus.BUSY));

        return new BookSlotResult(savedMeeting, updatedSlot);
    }
}
