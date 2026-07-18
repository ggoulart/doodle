package com.ggoulart.doodle.slot.domain;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class SlotTest {

    @Test
    void throwsWhenEndTimeIsNotAfterStartTime() {
        Instant start = Instant.parse("2026-07-20T10:00:00Z");

        assertThatThrownBy(() -> new Slot(UUID.randomUUID(), UUID.randomUUID(), start, start, SlotStatus.FREE))
                .isInstanceOf(InvalidTimeRangeException.class);
    }

    @Test
    void throwsWhenEndTimeIsBeforeStartTime() {
        Instant start = Instant.parse("2026-07-20T10:00:00Z");
        Instant end = Instant.parse("2026-07-20T09:00:00Z");

        assertThatThrownBy(() -> new Slot(UUID.randomUUID(), UUID.randomUUID(), start, end, SlotStatus.FREE))
                .isInstanceOf(InvalidTimeRangeException.class);
    }
}
