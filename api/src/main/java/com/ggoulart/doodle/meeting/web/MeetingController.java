package com.ggoulart.doodle.meeting.web;

import com.ggoulart.doodle.meeting.application.BookSlotCommand;
import com.ggoulart.doodle.meeting.application.BookSlotResult;
import com.ggoulart.doodle.meeting.application.BookSlotUseCase;
import com.ggoulart.doodle.meeting.application.DeleteMeetingUseCase;
import com.ggoulart.doodle.meeting.application.GetMeetingUseCase;
import com.ggoulart.doodle.meeting.domain.Meeting;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Meetings", description = "Book a slot as a meeting, look it up, and cancel it.")
public class MeetingController {

    private final BookSlotUseCase bookSlotUseCase;
    private final DeleteMeetingUseCase deleteMeetingUseCase;
    private final GetMeetingUseCase getMeetingUseCase;

    MeetingController(
            BookSlotUseCase bookSlotUseCase, DeleteMeetingUseCase deleteMeetingUseCase, GetMeetingUseCase getMeetingUseCase) {
        this.bookSlotUseCase = bookSlotUseCase;
        this.deleteMeetingUseCase = deleteMeetingUseCase;
        this.getMeetingUseCase = getMeetingUseCase;
    }

    @PostMapping("/slots/{id}/meetings")
    @Operation(summary = "Book a slot as a meeting")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Meeting booked, slot marked busy",
                    content = @Content(schema = @Schema(implementation = BookSlotResult.class))),
            @ApiResponse(responseCode = "400", description = "A participant email is invalid",
                    content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "404", description = "No slot with that id",
                    content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "409", description = "Slot is already booked, or isn't free",
                    content = @Content(schema = @Schema(implementation = String.class)))
    })
    public ResponseEntity<BookSlotResult> bookSlot(@PathVariable UUID id, @RequestBody BookSlotRequest request) {
        BookSlotCommand command = new BookSlotCommand(id, request.title(), request.description(), request.participants());
        BookSlotResult result = bookSlotUseCase.bookSlot(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @GetMapping("/meetings/{id}")
    @Operation(summary = "Get a meeting by id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Meeting found",
                    content = @Content(schema = @Schema(implementation = Meeting.class))),
            @ApiResponse(responseCode = "404", description = "No meeting with that id")
    })
    public ResponseEntity<Meeting> getMeeting(@PathVariable UUID id) {
        return getMeetingUseCase.getMeeting(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/meetings/{id}")
    @Operation(summary = "Cancel a meeting", description = "Deletes the meeting and frees its slot. A no-op if the meeting doesn't exist.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Meeting deleted (or already absent), slot freed")
    })
    public ResponseEntity<Void> deleteMeeting(@PathVariable UUID id) {
        deleteMeetingUseCase.deleteMeeting(id);
        return ResponseEntity.noContent().build();
    }
}
