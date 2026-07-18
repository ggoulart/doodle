package com.ggoulart.doodle.user.application;

import com.ggoulart.doodle.user.domain.User;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
class UserService implements CreateUserUseCase, GetUserUseCase {

    private final UserRepository userRepository;

    UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public User createUser(CreateUserCommand command) {
        User user = new User(UUID.randomUUID(), command.name(), command.email());
        return userRepository.save(user);
    }

    @Override
    public Optional<User> getUser(UUID id) {
        return userRepository.findById(id);
    }
}
