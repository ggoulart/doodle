package com.ggoulart.doodle.slot.application;

import com.ggoulart.doodle.slot.domain.Slot;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SlotRepository {

    Slot save(Slot slot);

    Optional<Slot> findById(UUID id);

    List<Slot> findByCalendarIdAndOverlapping(UUID calendarId, Instant from, Instant to);

    void deleteById(UUID id);
}
