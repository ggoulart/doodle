package com.ggoulart.doodle.meeting.web;

import com.ggoulart.doodle.meeting.application.BookSlotCommand;
import com.ggoulart.doodle.meeting.application.BookSlotResult;
import com.ggoulart.doodle.meeting.application.BookSlotUseCase;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/slots/{id}/meetings")
public class MeetingController {

    private final BookSlotUseCase bookSlotUseCase;

    MeetingController(BookSlotUseCase bookSlotUseCase) {
        this.bookSlotUseCase = bookSlotUseCase;
    }

    @PostMapping
    public ResponseEntity<BookSlotResult> bookSlot(@PathVariable UUID id, @RequestBody BookSlotRequest request) {
        BookSlotCommand command = new BookSlotCommand(id, request.title(), request.description(), request.participants());
        BookSlotResult result = bookSlotUseCase.bookSlot(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }
}
