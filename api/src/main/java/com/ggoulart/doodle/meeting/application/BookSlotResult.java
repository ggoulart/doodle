package com.ggoulart.doodle.meeting.application;

import com.ggoulart.doodle.meeting.domain.Meeting;
import com.ggoulart.doodle.slot.domain.Slot;

public record BookSlotResult(Meeting meeting, Slot slot) {
}
