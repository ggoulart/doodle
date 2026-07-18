package com.ggoulart.doodle.user.application;

import com.ggoulart.doodle.calendar.application.CreateCalendarUseCase;
import com.ggoulart.doodle.user.domain.User;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class UserService implements CreateUserUseCase, GetUserUseCase {

    private final UserRepository userRepository;
    private final CreateCalendarUseCase createCalendarUseCase;

    UserService(UserRepository userRepository, CreateCalendarUseCase createCalendarUseCase) {
        this.userRepository = userRepository;
        this.createCalendarUseCase = createCalendarUseCase;
    }

    @Transactional
    @Override
    public User createUser(CreateUserCommand command) {
        User user = new User(UUID.randomUUID(), command.name(), command.email());
        User savedUser = userRepository.save(user);
        createCalendarUseCase.createCalendar(savedUser.id());
        return savedUser;
    }

    @Override
    public Optional<User> getUser(UUID id) {
        return userRepository.findById(id);
    }
}
