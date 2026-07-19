package com.ggoulart.doodle.meeting.domain;

public class InvalidParticipantException extends RuntimeException {

    public InvalidParticipantException(String message) {
        super(message);
    }
}
