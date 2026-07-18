package com.ggoulart.doodle.user.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ggoulart.doodle.user.application.CreateUserCommand;
import com.ggoulart.doodle.user.application.CreateUserUseCase;
import com.ggoulart.doodle.user.application.GetUserUseCase;
import com.ggoulart.doodle.user.domain.User;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CreateUserUseCase createUserUseCase;

    @MockitoBean
    private GetUserUseCase getUserUseCase;

    @Test
    void createUserReturnsCreatedUser() throws Exception {
        CreateUserRequest request = new CreateUserRequest("Ada Lovelace", "ada@example.com");
        User created = new User(UUID.randomUUID(), request.name(), request.email());
        when(createUserUseCase.createUser(any(CreateUserCommand.class))).thenReturn(created);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(created.id().toString()))
                .andExpect(jsonPath("$.name").value("Ada Lovelace"))
                .andExpect(jsonPath("$.email").value("ada@example.com"));
    }

    @Test
    void getUserReturnsUserWhenFound() throws Exception {
        User user = new User(UUID.randomUUID(), "Ada Lovelace", "ada@example.com");
        when(getUserUseCase.getUser(user.id())).thenReturn(Optional.of(user));

        mockMvc.perform(get("/users/{id}", user.id()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(user.id().toString()))
                .andExpect(jsonPath("$.name").value("Ada Lovelace"))
                .andExpect(jsonPath("$.email").value("ada@example.com"));
    }

    @Test
    void getUserReturnsNotFoundWhenMissing() throws Exception {
        UUID id = UUID.randomUUID();
        when(getUserUseCase.getUser(id)).thenReturn(Optional.empty());

        mockMvc.perform(get("/users/{id}", id))
                .andExpect(status().isNotFound());
    }
}
