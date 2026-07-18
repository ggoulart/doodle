package com.ggoulart.doodle.slot.application;

import com.ggoulart.doodle.slot.domain.Slot;

public interface SlotRepository {

    Slot save(Slot slot);
}
