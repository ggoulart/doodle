package com.ggoulart.doodle.user.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ggoulart.doodle.calendar.application.CreateCalendarUseCase;
import com.ggoulart.doodle.user.domain.User;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private CreateCalendarUseCase createCalendarUseCase;

    @Test
    void createUserSavesUserAndCreatesItsCalendar() {
        UserService service = new UserService(userRepository, createCalendarUseCase);
        CreateUserCommand command = new CreateUserCommand("Ada Lovelace", "ada@example.com");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User created = service.createUser(command);

        assertThat(created.name()).isEqualTo("Ada Lovelace");
        assertThat(created.email()).isEqualTo("ada@example.com");
        assertThat(created.id()).isNotNull();
        verify(createCalendarUseCase).createCalendar(created.id());
    }

    @Test
    void getUserReturnsUserWhenFound() {
        UserService service = new UserService(userRepository, createCalendarUseCase);
        User user = new User(UUID.randomUUID(), "Ada Lovelace", "ada@example.com");
        when(userRepository.findById(user.id())).thenReturn(Optional.of(user));

        Optional<User> result = service.getUser(user.id());

        assertThat(result).contains(user);
    }

    @Test
    void getUserReturnsEmptyWhenMissing() {
        UserService service = new UserService(userRepository, createCalendarUseCase);
        UUID id = UUID.randomUUID();
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        Optional<User> result = service.getUser(id);

        assertThat(result).isEmpty();
    }
}
