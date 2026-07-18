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
    public ResponseEntity<Slot> createSlot(@RequestBody CreateSlotRequest request) {
        CreateSlotCommand command = new CreateSlotCommand(
                request.userId(), request.startTime(), request.endTime(), request.status());
        Slot slot = createSlotUseCase.createSlot(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(slot);
    }

    @GetMapping
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
    public ResponseEntity<Slot> updateSlot(@PathVariable UUID id, @RequestBody UpdateSlotRequest request) {
        UpdateSlotCommand command = new UpdateSlotCommand(id, request.startTime(), request.endTime(), request.status());
        Slot slot = updateSlotUseCase.updateSlot(command);
        return ResponseEntity.ok(slot);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSlot(@PathVariable UUID id) {
        deleteSlotUseCase.deleteSlot(id);
        return ResponseEntity.noContent().build();
    }
}
