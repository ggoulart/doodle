package com.ggoulart.doodle.meeting.web;

import com.ggoulart.doodle.meeting.application.BookSlotCommand;
import com.ggoulart.doodle.meeting.application.BookSlotResult;
import com.ggoulart.doodle.meeting.application.BookSlotUseCase;
import com.ggoulart.doodle.meeting.application.DeleteMeetingUseCase;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MeetingController {

    private final BookSlotUseCase bookSlotUseCase;
    private final DeleteMeetingUseCase deleteMeetingUseCase;

    MeetingController(BookSlotUseCase bookSlotUseCase, DeleteMeetingUseCase deleteMeetingUseCase) {
        this.bookSlotUseCase = bookSlotUseCase;
        this.deleteMeetingUseCase = deleteMeetingUseCase;
    }

    @PostMapping("/slots/{id}/meetings")
    public ResponseEntity<BookSlotResult> bookSlot(@PathVariable UUID id, @RequestBody BookSlotRequest request) {
        BookSlotCommand command = new BookSlotCommand(id, request.title(), request.description(), request.participants());
        BookSlotResult result = bookSlotUseCase.bookSlot(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @DeleteMapping("/meetings/{id}")
    public ResponseEntity<Void> deleteMeeting(@PathVariable UUID id) {
        deleteMeetingUseCase.deleteMeeting(id);
        return ResponseEntity.noContent().build();
    }
}
