package com.ggoulart.doodle.meeting.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class MeetingTest {

    @Test
    void keepsGivenTitleWhenNotBlank() {
        Meeting meeting = new Meeting(UUID.randomUUID(), UUID.randomUUID(), "Planning", null, List.of());

        assertThat(meeting.title()).isEqualTo("Planning");
    }

    @Test
    void defaultsTitleWhenBlank() {
        Meeting meeting = new Meeting(UUID.randomUUID(), UUID.randomUUID(), "   ", null, List.of());

        assertThat(meeting.title()).isEqualTo("(No title)");
    }

    @Test
    void defaultsTitleWhenNull() {
        Meeting meeting = new Meeting(UUID.randomUUID(), UUID.randomUUID(), null, null, List.of());

        assertThat(meeting.title()).isEqualTo("(No title)");
    }
}
