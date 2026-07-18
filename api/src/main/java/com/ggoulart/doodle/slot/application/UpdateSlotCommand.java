package com.ggoulart.doodle.slot.application;

import com.ggoulart.doodle.slot.domain.SlotStatus;
import java.time.Instant;
import java.util.UUID;

public record UpdateSlotCommand(UUID id, Instant startTime, Instant endTime, SlotStatus status) {
}
