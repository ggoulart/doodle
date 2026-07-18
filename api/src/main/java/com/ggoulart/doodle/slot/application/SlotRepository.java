package com.ggoulart.doodle.slot.application;

import com.ggoulart.doodle.slot.domain.Slot;
import com.ggoulart.doodle.slot.domain.SlotStatus;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SlotRepository {

    Slot save(Slot slot);

    Optional<Slot> findById(UUID id);

    List<Slot> findByCalendarIdAndOverlapping(UUID calendarId, Instant from, Instant to, SlotStatus status);

    void deleteById(UUID id);
}
