package com.ggoulart.doodle.slot.application;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.ggoulart.doodle.slot.domain.InvalidTimeRangeException;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class QuerySlotsCommandTest {

    @Test
    void throwsWhenToIsNotAfterFrom() {
        Instant from = Instant.parse("2026-07-20T00:00:00Z");

        assertThatThrownBy(() -> new QuerySlotsCommand(UUID.randomUUID(), from, from, null))
                .isInstanceOf(InvalidTimeRangeException.class);
    }

    @Test
    void throwsWhenToIsBeforeFrom() {
        Instant from = Instant.parse("2026-07-20T00:00:00Z");
        Instant to = Instant.parse("2026-07-19T00:00:00Z");

        assertThatThrownBy(() -> new QuerySlotsCommand(UUID.randomUUID(), from, to, null))
                .isInstanceOf(InvalidTimeRangeException.class);
    }
}
