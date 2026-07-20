package com.ggoulart.doodle.slot.web;

import com.ggoulart.doodle.slot.application.CreateSlotCommand;
import com.ggoulart.doodle.slot.application.CreateSlotUseCase;
import com.ggoulart.doodle.slot.application.DeleteSlotUseCase;
import com.ggoulart.doodle.slot.application.QuerySlotsCommand;
import com.ggoulart.doodle.slot.application.QuerySlotsUseCase;
import com.ggoulart.doodle.slot.application.UpdateSlotCommand;
import com.ggoulart.doodle.slot.application.UpdateSlotUseCase;
import com.ggoulart.doodle.slot.domain.Slot;
import com.ggoulart.doodle.slot.domain.SlotStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/slots")
@Tag(name = "Slots", description = "Create, query, update, and delete time slots within a user's calendar.")
public class SlotController {

    private static final Duration DEFAULT_RANGE = Duration.ofDays(7);

    private final CreateSlotUseCase createSlotUseCase;
    private final DeleteSlotUseCase deleteSlotUseCase;
    private final UpdateSlotUseCase updateSlotUseCase;
    private final QuerySlotsUseCase querySlotsUseCase;

    SlotController(
            CreateSlotUseCase createSlotUseCase,
            DeleteSlotUseCase deleteSlotUseCase,
            UpdateSlotUseCase updateSlotUseCase,
            QuerySlotsUseCase querySlotsUseCase) {
        this.createSlotUseCase = createSlotUseCase;
        this.deleteSlotUseCase = deleteSlotUseCase;
        this.updateSlotUseCase = updateSlotUseCase;
        this.querySlotsUseCase = querySlotsUseCase;
    }

    @PostMapping
    @Operation(summary = "Create a slot", description = "Creates a slot in the given user's calendar.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Slot created",
                    content = @Content(schema = @Schema(implementation = Slot.class))),
            @ApiResponse(responseCode = "400", description = "Invalid time range, or the user/calendar doesn't exist",
                    content = @Content(schema = @Schema(implementation = String.class)))
    })
    public ResponseEntity<Slot> createSlot(@RequestBody CreateSlotRequest request) {
        CreateSlotCommand command = new CreateSlotCommand(
                request.userId(), request.startTime(), request.endTime(), request.status());
        Slot slot = createSlotUseCase.createSlot(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(slot);
    }

    @GetMapping
    @Operation(summary = "Query slots", description = "Lists a user's slots overlapping [from, to) — defaulting to the next 7 days when omitted — optionally filtered by status.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Slots found (possibly empty)",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = Slot.class)))),
            @ApiResponse(responseCode = "400", description = "Invalid time range, or the user/calendar doesn't exist",
                    content = @Content(schema = @Schema(implementation = String.class)))
    })
    public ResponseEntity<List<Slot>> querySlots(
            @RequestParam UUID userId,
            @RequestParam(required = false) Instant from,
            @RequestParam(required = false) Instant to,
            @RequestParam(required = false) SlotStatus status) {
        Instant resolvedFrom = from != null ? from : Instant.now();
        Instant resolvedTo = to != null ? to : resolvedFrom.plus(DEFAULT_RANGE);
        QuerySlotsCommand command = new QuerySlotsCommand(userId, resolvedFrom, resolvedTo, status);
        return ResponseEntity.ok(querySlotsUseCase.querySlots(command));
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Update a slot", description = "Partially updates a slot's time range and/or status. Rejected if the slot currently has a meeting booked.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Slot updated",
                    content = @Content(schema = @Schema(implementation = Slot.class))),
            @ApiResponse(responseCode = "400", description = "Invalid resulting time range",
                    content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "404", description = "No slot with that id",
                    content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "409", description = "Slot has a meeting booked and can't be modified",
                    content = @Content(schema = @Schema(implementation = String.class)))
    })
    public ResponseEntity<Slot> updateSlot(@PathVariable UUID id, @RequestBody UpdateSlotRequest request) {
        UpdateSlotCommand command = new UpdateSlotCommand(id, request.startTime(), request.endTime(), request.status());
        Slot slot = updateSlotUseCase.updateSlot(command);
        return ResponseEntity.ok(slot);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a slot")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Slot deleted (or already absent)"),
            @ApiResponse(responseCode = "409", description = "Slot has a meeting booked and can't be deleted",
                    content = @Content(schema = @Schema(implementation = String.class)))
    })
    public ResponseEntity<Void> deleteSlot(@PathVariable UUID id) {
        deleteSlotUseCase.deleteSlot(id);
        return ResponseEntity.noContent().build();
    }
}
