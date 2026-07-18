package com.ggoulart.doodle.user.application;

import com.ggoulart.doodle.user.domain.User;
import java.util.Optional;
import java.util.UUID;

public interface GetUserUseCase {

    Optional<User> getUser(UUID id);
}
