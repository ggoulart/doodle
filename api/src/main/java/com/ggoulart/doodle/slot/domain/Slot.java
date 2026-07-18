package com.ggoulart.doodle.slot.domain;

import java.time.Instant;
import java.util.UUID;

public record Slot(UUID id, UUID calendarId, Instant startTime, Instant endTime, SlotStatus status) {
}
