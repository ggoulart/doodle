package com.ggoulart.doodle.user;

import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
class UserService implements CreateUserUseCase {

    private final UserRepository userRepository;

    UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public User createUser(CreateUserRequest request) {
        User user = new User(UUID.randomUUID(), request.name(), request.email());
        return userRepository.save(user);
    }
}
