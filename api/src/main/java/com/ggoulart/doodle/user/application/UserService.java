package com.ggoulart.doodle.user.application;

import com.ggoulart.doodle.user.domain.User;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
class UserService implements CreateUserUseCase {

    private final UserRepository userRepository;

    UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public User createUser(CreateUserCommand command) {
        User user = new User(UUID.randomUUID(), command.name(), command.email());
        return userRepository.save(user);
    }
}
