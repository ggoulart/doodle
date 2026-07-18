package com.ggoulart.doodle.slot.application;

import com.ggoulart.doodle.slot.domain.Slot;
import java.util.UUID;

public interface SlotRepository {

    Slot save(Slot slot);

    void deleteById(UUID id);
}
