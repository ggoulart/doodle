package com.ggoulart.doodle.slot.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ggoulart.doodle.slot.application.CalendarNotFoundException;
import com.ggoulart.doodle.slot.application.CreateSlotCommand;
import com.ggoulart.doodle.slot.application.CreateSlotUseCase;
import com.ggoulart.doodle.slot.application.DeleteSlotUseCase;
import com.ggoulart.doodle.slot.application.InvalidTimeRangeException;
import com.ggoulart.doodle.slot.application.SlotNotFoundException;
import com.ggoulart.doodle.slot.application.UpdateSlotCommand;
import com.ggoulart.doodle.slot.application.UpdateSlotUseCase;
import com.ggoulart.doodle.slot.application.UserNotFoundException;
import com.ggoulart.doodle.slot.domain.Slot;
import com.ggoulart.doodle.slot.domain.SlotStatus;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

@WebMvcTest(SlotController.class)
class SlotControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CreateSlotUseCase createSlotUseCase;

    @MockitoBean
    private DeleteSlotUseCase deleteSlotUseCase;

    @MockitoBean
    private UpdateSlotUseCase updateSlotUseCase;

    private CreateSlotRequest sampleRequest() {
        return new CreateSlotRequest(
                UUID.randomUUID(),
                Instant.parse("2026-07-20T10:00:00Z"),
                Instant.parse("2026-07-20T10:30:00Z"),
                SlotStatus.FREE);
    }

    @Test
    void createSlotReturnsCreatedSlot() throws Exception {
        CreateSlotRequest request = sampleRequest();
        Slot created = new Slot(UUID.randomUUID(), UUID.randomUUID(), request.startTime(), request.endTime(), SlotStatus.FREE);
        when(createSlotUseCase.createSlot(any(CreateSlotCommand.class))).thenReturn(created);

        mockMvc.perform(post("/slots")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(created.id().toString()))
                .andExpect(jsonPath("$.calendarId").value(created.calendarId().toString()))
                .andExpect(jsonPath("$.status").value("FREE"));
    }

    @Test
    void createSlotReturnsBadRequestForInvalidTimeRange() throws Exception {
        when(createSlotUseCase.createSlot(any(CreateSlotCommand.class)))
                .thenThrow(new InvalidTimeRangeException("endTime must be after startTime"));

        mockMvc.perform(post("/slots")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleRequest())))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createSlotReturnsBadRequestWhenUserNotFound() throws Exception {
        when(createSlotUseCase.createSlot(any(CreateSlotCommand.class)))
                .thenThrow(new UserNotFoundException(UUID.randomUUID()));

        mockMvc.perform(post("/slots")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleRequest())))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createSlotReturnsBadRequestWhenCalendarNotFound() throws Exception {
        when(createSlotUseCase.createSlot(any(CreateSlotCommand.class)))
                .thenThrow(new CalendarNotFoundException(UUID.randomUUID()));

        mockMvc.perform(post("/slots")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleRequest())))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteSlotReturnsNoContent() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(delete("/slots/{id}", id))
                .andExpect(status().isNoContent());

        verify(deleteSlotUseCase).deleteSlot(id);
    }

    @Test
    void updateSlotReturnsUpdatedSlot() throws Exception {
        UUID id = UUID.randomUUID();
        UpdateSlotRequest request = new UpdateSlotRequest(null, null, SlotStatus.BUSY);
        Slot updated = new Slot(
                id, UUID.randomUUID(), Instant.parse("2026-07-20T10:00:00Z"), Instant.parse("2026-07-20T10:30:00Z"), SlotStatus.BUSY);
        when(updateSlotUseCase.updateSlot(any(UpdateSlotCommand.class))).thenReturn(updated);

        mockMvc.perform(patch("/slots/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.status").value("BUSY"));
    }

    @Test
    void updateSlotReturnsNotFoundWhenSlotDoesNotExist() throws Exception {
        UUID id = UUID.randomUUID();
        UpdateSlotRequest request = new UpdateSlotRequest(null, null, SlotStatus.BUSY);
        when(updateSlotUseCase.updateSlot(any(UpdateSlotCommand.class))).thenThrow(new SlotNotFoundException(id));

        mockMvc.perform(patch("/slots/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateSlotReturnsBadRequestForInvalidTimeRange() throws Exception {
        UUID id = UUID.randomUUID();
        UpdateSlotRequest request = new UpdateSlotRequest(Instant.parse("2026-07-20T11:00:00Z"), null, null);
        when(updateSlotUseCase.updateSlot(any(UpdateSlotCommand.class)))
                .thenThrow(new InvalidTimeRangeException("endTime must be after startTime"));

        mockMvc.perform(patch("/slots/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
