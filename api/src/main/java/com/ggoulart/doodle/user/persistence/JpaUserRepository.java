package com.ggoulart.doodle.user.persistence;

import com.ggoulart.doodle.user.User;
import com.ggoulart.doodle.user.UserRepository;
import org.springframework.stereotype.Repository;

@Repository
class JpaUserRepository implements UserRepository {

    private final UserJpaRepository userJpaRepository;

    JpaUserRepository(UserJpaRepository userJpaRepository) {
        this.userJpaRepository = userJpaRepository;
    }

    @Override
    public User save(User user) {
        UserEntity entity = new UserEntity(user.id(), user.name(), user.email());
        UserEntity saved = userJpaRepository.save(entity);
        return new User(saved.getId(), saved.getName(), saved.getEmail());
    }
}
