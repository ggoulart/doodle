package com.ggoulart.doodle.slot.web;

import com.ggoulart.doodle.slot.domain.SlotStatus;
import java.time.Instant;
import java.util.UUID;

public record CreateSlotRequest(UUID userId, Instant startTime, Instant endTime, SlotStatus status) {
}
