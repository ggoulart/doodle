package com.ggoulart.doodle.meeting.domain;

import java.util.List;
import java.util.UUID;

public record Meeting(UUID id, UUID slotId, String title, String description, List<String> participants) {
}
