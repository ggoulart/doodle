package com.ggoulart.doodle.slot.web;

import com.ggoulart.doodle.slot.application.CalendarNotFoundException;
import com.ggoulart.doodle.slot.application.SlotNotFoundException;
import com.ggoulart.doodle.slot.application.UserNotFoundException;
import com.ggoulart.doodle.slot.domain.InvalidTimeRangeException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = SlotController.class)
class SlotExceptionHandler {

    @ExceptionHandler(InvalidTimeRangeException.class)
    ResponseEntity<String> handleInvalidTimeRange(InvalidTimeRangeException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(UserNotFoundException.class)
    ResponseEntity<String> handleUserNotFound(UserNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(CalendarNotFoundException.class)
    ResponseEntity<String> handleCalendarNotFound(CalendarNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(SlotNotFoundException.class)
    ResponseEntity<String> handleSlotNotFound(SlotNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }
}
