package com.ggoulart.doodle.slot.application;

import com.ggoulart.doodle.slot.domain.Slot;
import java.util.Optional;
import java.util.UUID;

public interface GetSlotUseCase {

    Optional<Slot> getSlot(UUID id);
}
