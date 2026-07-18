package com.ggoulart.doodle.user.application;

import com.ggoulart.doodle.user.domain.User;

public interface CreateUserUseCase {

    User createUser(CreateUserCommand command);
}
