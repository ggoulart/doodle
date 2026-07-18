package com.ggoulart.doodle.meeting.web;

import java.util.List;

public record BookSlotRequest(String title, String description, List<String> participants) {
}
