package com.ggoulart.doodle.slot.web;

import com.ggoulart.doodle.slot.domain.SlotStatus;
import java.time.Instant;

public record UpdateSlotRequest(Instant startTime, Instant endTime, SlotStatus status) {
}
