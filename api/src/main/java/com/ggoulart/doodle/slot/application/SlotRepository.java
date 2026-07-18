package com.ggoulart.doodle.slot.application;

import com.ggoulart.doodle.slot.domain.Slot;
import java.util.Optional;
import java.util.UUID;

public interface SlotRepository {

    Slot save(Slot slot);

    Optional<Slot> findById(UUID id);

    void deleteById(UUID id);
}
