package com.ggoulart.doodle.user.application;

import com.ggoulart.doodle.user.domain.User;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository {

    User save(User user);

    Optional<User> findById(UUID id);

    boolean existsByEmail(String email);
}
