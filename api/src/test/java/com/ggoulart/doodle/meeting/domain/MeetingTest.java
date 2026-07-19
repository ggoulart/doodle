package com.ggoulart.doodle.meeting.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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

    @Test
    void trimsAndLowercasesParticipants() {
        Meeting meeting = new Meeting(
                UUID.randomUUID(), UUID.randomUUID(), "Planning", null, List.of("  Ada@Example.com  "));

        assertThat(meeting.participants()).containsExactly("ada@example.com");
    }

    @Test
    void treatsNullParticipantsAsEmptyList() {
        Meeting meeting = new Meeting(UUID.randomUUID(), UUID.randomUUID(), "Planning", null, null);

        assertThat(meeting.participants()).isEmpty();
    }

    @Test
    void throwsWhenParticipantIsNotAValidEmail() {
        assertThatThrownBy(() -> new Meeting(
                        UUID.randomUUID(), UUID.randomUUID(), "Planning", null, List.of("not-an-email")))
                .isInstanceOf(InvalidParticipantException.class);
    }

    @Test
    void throwsWhenParticipantIsBlank() {
        assertThatThrownBy(() -> new Meeting(
                        UUID.randomUUID(), UUID.randomUUID(), "Planning", null, List.of("   ")))
                .isInstanceOf(InvalidParticipantException.class);
    }
}
