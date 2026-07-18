package com.ggoulart.doodle.user.application;

import com.ggoulart.doodle.user.domain.User;

public interface UserRepository {

    User save(User user);
}
