package com.ggoulart.doodle.meeting.domain;

import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Pattern;

public record Meeting(UUID id, UUID slotId, String title, String description, List<String> participants) {

    private static final String DEFAULT_TITLE = "(No title)";
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    public Meeting {
        title = title == null || title.isBlank() ? DEFAULT_TITLE : title;

        List<String> normalizedParticipants = (participants == null ? List.<String>of() : participants).stream()
                .map(participant -> participant == null ? "" : participant.trim().toLowerCase(Locale.ROOT))
                .toList();
        for (String participant : normalizedParticipants) {
            if (!EMAIL_PATTERN.matcher(participant).matches()) {
                throw new InvalidParticipantException("participant is not a valid email: " + participant);
            }
        }
        participants = normalizedParticipants;
    }
}
