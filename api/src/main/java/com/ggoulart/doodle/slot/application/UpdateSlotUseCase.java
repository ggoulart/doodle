package com.ggoulart.doodle.slot.application;

import com.ggoulart.doodle.slot.domain.Slot;

public interface UpdateSlotUseCase {

    Slot updateSlot(UpdateSlotCommand command);
}
