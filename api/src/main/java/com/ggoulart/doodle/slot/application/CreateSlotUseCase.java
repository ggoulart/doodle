package com.ggoulart.doodle.slot.application;

import com.ggoulart.doodle.slot.domain.Slot;

public interface CreateSlotUseCase {

    Slot createSlot(CreateSlotCommand command);
}
