package com.ggoulart.doodle.slot.web;

import com.ggoulart.doodle.slot.application.CreateSlotCommand;
import com.ggoulart.doodle.slot.application.CreateSlotUseCase;
import com.ggoulart.doodle.slot.domain.Slot;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/slots")
public class SlotController {

    private final CreateSlotUseCase createSlotUseCase;

    SlotController(CreateSlotUseCase createSlotUseCase) {
        this.createSlotUseCase = createSlotUseCase;
    }

    @PostMapping
    public ResponseEntity<Slot> createSlot(@RequestBody CreateSlotRequest request) {
        CreateSlotCommand command = new CreateSlotCommand(
                request.userId(), request.startTime(), request.endTime(), request.status());
        Slot slot = createSlotUseCase.createSlot(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(slot);
    }
}
