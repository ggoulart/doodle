package com.ggoulart.doodle.slot.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ggoulart.doodle.slot.application.CalendarNotFoundException;
import com.ggoulart.doodle.slot.application.CreateSlotCommand;
import com.ggoulart.doodle.slot.application.CreateSlotUseCase;
import com.ggoulart.doodle.slot.application.InvalidTimeRangeException;
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
}
