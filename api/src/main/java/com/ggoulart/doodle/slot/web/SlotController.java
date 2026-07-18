package com.ggoulart.doodle.slot.web;

import com.ggoulart.doodle.slot.application.CreateSlotCommand;
import com.ggoulart.doodle.slot.application.CreateSlotUseCase;
import com.ggoulart.doodle.slot.application.DeleteSlotUseCase;
import com.ggoulart.doodle.slot.application.UpdateSlotCommand;
import com.ggoulart.doodle.slot.application.UpdateSlotUseCase;
import com.ggoulart.doodle.slot.domain.Slot;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/slots")
public class SlotController {

    private final CreateSlotUseCase createSlotUseCase;
    private final DeleteSlotUseCase deleteSlotUseCase;
    private final UpdateSlotUseCase updateSlotUseCase;

    SlotController(
            CreateSlotUseCase createSlotUseCase,
            DeleteSlotUseCase deleteSlotUseCase,
            UpdateSlotUseCase updateSlotUseCase) {
        this.createSlotUseCase = createSlotUseCase;
        this.deleteSlotUseCase = deleteSlotUseCase;
        this.updateSlotUseCase = updateSlotUseCase;
    }

    @PostMapping
    public ResponseEntity<Slot> createSlot(@RequestBody CreateSlotRequest request) {
        CreateSlotCommand command = new CreateSlotCommand(
                request.userId(), request.startTime(), request.endTime(), request.status());
        Slot slot = createSlotUseCase.createSlot(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(slot);
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
