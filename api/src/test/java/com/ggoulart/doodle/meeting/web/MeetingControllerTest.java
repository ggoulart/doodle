package com.ggoulart.doodle.meeting.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ggoulart.doodle.meeting.application.BookSlotCommand;
import com.ggoulart.doodle.meeting.application.BookSlotResult;
import com.ggoulart.doodle.meeting.application.BookSlotUseCase;
import com.ggoulart.doodle.meeting.application.DeleteMeetingUseCase;
import com.ggoulart.doodle.meeting.application.GetMeetingUseCase;
import com.ggoulart.doodle.meeting.application.MeetingAlreadyExistsException;
import com.ggoulart.doodle.meeting.application.SlotNotFoundException;
import com.ggoulart.doodle.meeting.application.SlotNotFreeException;
import com.ggoulart.doodle.meeting.domain.InvalidParticipantException;
import com.ggoulart.doodle.meeting.domain.Meeting;
import com.ggoulart.doodle.slot.domain.Slot;
import com.ggoulart.doodle.slot.domain.SlotStatus;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

@WebMvcTest(MeetingController.class)
class MeetingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private BookSlotUseCase bookSlotUseCase;

    @MockitoBean
    private DeleteMeetingUseCase deleteMeetingUseCase;

    @MockitoBean
    private GetMeetingUseCase getMeetingUseCase;

    private BookSlotRequest sampleRequest() {
        return new BookSlotRequest("Planning", "Sprint planning", List.of("ada@example.com"));
    }

    @Test
    void bookSlotReturnsCreatedMeetingAndUpdatedSlot() throws Exception {
        UUID slotId = UUID.randomUUID();
        Meeting meeting = new Meeting(UUID.randomUUID(), slotId, "Planning", "Sprint planning", List.of("ada@example.com"));
        Slot slot = new Slot(
                slotId, UUID.randomUUID(), Instant.parse("2026-07-20T10:00:00Z"), Instant.parse("2026-07-20T10:30:00Z"), SlotStatus.BUSY);
        when(bookSlotUseCase.bookSlot(any(BookSlotCommand.class))).thenReturn(new BookSlotResult(meeting, slot));

        mockMvc.perform(post("/slots/{id}/meetings", slotId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.meeting.title").value("Planning"))
                .andExpect(jsonPath("$.meeting.participants[0]").value("ada@example.com"))
                .andExpect(jsonPath("$.slot.status").value("BUSY"));
    }

    @Test
    void bookSlotReturnsNotFoundWhenSlotDoesNotExist() throws Exception {
        UUID slotId = UUID.randomUUID();
        when(bookSlotUseCase.bookSlot(any(BookSlotCommand.class))).thenThrow(new SlotNotFoundException(slotId));

        mockMvc.perform(post("/slots/{id}/meetings", slotId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleRequest())))
                .andExpect(status().isNotFound());
    }

    @Test
    void bookSlotReturnsConflictWhenMeetingAlreadyExists() throws Exception {
        UUID slotId = UUID.randomUUID();
        when(bookSlotUseCase.bookSlot(any(BookSlotCommand.class))).thenThrow(new MeetingAlreadyExistsException(slotId));

        mockMvc.perform(post("/slots/{id}/meetings", slotId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleRequest())))
                .andExpect(status().isConflict());
    }

    @Test
    void bookSlotReturnsConflictWhenSlotIsNotFree() throws Exception {
        UUID slotId = UUID.randomUUID();
        when(bookSlotUseCase.bookSlot(any(BookSlotCommand.class))).thenThrow(new SlotNotFreeException(slotId));

        mockMvc.perform(post("/slots/{id}/meetings", slotId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleRequest())))
                .andExpect(status().isConflict());
    }

    @Test
    void bookSlotReturnsBadRequestWhenParticipantIsInvalid() throws Exception {
        UUID slotId = UUID.randomUUID();
        when(bookSlotUseCase.bookSlot(any(BookSlotCommand.class)))
                .thenThrow(new InvalidParticipantException("participant is not a valid email: not-an-email"));

        mockMvc.perform(post("/slots/{id}/meetings", slotId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleRequest())))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteMeetingReturnsNoContent() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(delete("/meetings/{id}", id))
                .andExpect(status().isNoContent());

        verify(deleteMeetingUseCase).deleteMeeting(id);
    }

    @Test
    void getMeetingReturnsMeetingWhenFound() throws Exception {
        Meeting meeting = new Meeting(UUID.randomUUID(), UUID.randomUUID(), "Planning", "Sprint planning", List.of("ada@example.com"));
        when(getMeetingUseCase.getMeeting(meeting.id())).thenReturn(Optional.of(meeting));

        mockMvc.perform(get("/meetings/{id}", meeting.id()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(meeting.id().toString()))
                .andExpect(jsonPath("$.title").value("Planning"))
                .andExpect(jsonPath("$.participants[0]").value("ada@example.com"));
    }

    @Test
    void getMeetingReturnsNotFoundWhenMissing() throws Exception {
        UUID id = UUID.randomUUID();
        when(getMeetingUseCase.getMeeting(id)).thenReturn(Optional.empty());

        mockMvc.perform(get("/meetings/{id}", id))
                .andExpect(status().isNotFound());
    }
}
