package com.ggoulart.doodle.meeting.application;

import java.util.List;
import java.util.UUID;

public record BookSlotCommand(UUID slotId, String title, String description, List<String> participants) {
}
