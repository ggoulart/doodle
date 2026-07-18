package com.ggoulart.doodle.slot.web;

import com.ggoulart.doodle.slot.application.CreateSlotCommand;
import com.ggoulart.doodle.slot.application.CreateSlotUseCase;
import com.ggoulart.doodle.slot.application.DeleteSlotUseCase;
import com.ggoulart.doodle.slot.domain.Slot;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
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

    SlotController(CreateSlotUseCase createSlotUseCase, DeleteSlotUseCase deleteSlotUseCase) {
        this.createSlotUseCase = createSlotUseCase;
        this.deleteSlotUseCase = deleteSlotUseCase;
    }

    @PostMapping
    public ResponseEntity<Slot> createSlot(@RequestBody CreateSlotRequest request) {
        CreateSlotCommand command = new CreateSlotCommand(
                request.userId(), request.startTime(), request.endTime(), request.status());
        Slot slot = createSlotUseCase.createSlot(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(slot);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSlot(@PathVariable UUID id) {
        deleteSlotUseCase.deleteSlot(id);
        return ResponseEntity.noContent().build();
    }
}
