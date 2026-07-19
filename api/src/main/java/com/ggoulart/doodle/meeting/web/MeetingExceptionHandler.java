package com.ggoulart.doodle.meeting.web;

import com.ggoulart.doodle.meeting.application.MeetingAlreadyExistsException;
import com.ggoulart.doodle.meeting.application.SlotNotFoundException;
import com.ggoulart.doodle.meeting.application.SlotNotFreeException;
import com.ggoulart.doodle.meeting.domain.InvalidParticipantException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = MeetingController.class)
class MeetingExceptionHandler {

    @ExceptionHandler(SlotNotFoundException.class)
    ResponseEntity<String> handleSlotNotFound(SlotNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(MeetingAlreadyExistsException.class)
    ResponseEntity<String> handleMeetingAlreadyExists(MeetingAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }

    @ExceptionHandler(SlotNotFreeException.class)
    ResponseEntity<String> handleSlotNotFree(SlotNotFreeException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }

    @ExceptionHandler(InvalidParticipantException.class)
    ResponseEntity<String> handleInvalidParticipant(InvalidParticipantException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }
}
