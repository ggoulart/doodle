package com.ggoulart.doodle.user.persistence;

import com.ggoulart.doodle.user.application.DuplicateEmailException;
import com.ggoulart.doodle.user.application.UserRepository;
import com.ggoulart.doodle.user.domain.User;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;

@Repository
class JpaUserRepository implements UserRepository {

    private static final String EMAIL_UNIQUE_CONSTRAINT = "uk_users_email";

    private final UserJpaRepository userJpaRepository;

    JpaUserRepository(UserJpaRepository userJpaRepository) {
        this.userJpaRepository = userJpaRepository;
    }

    @Override
    public User save(User user) {
        UserEntity entity = new UserEntity(user.id(), user.name(), user.email());
        try {
            UserEntity saved = userJpaRepository.saveAndFlush(entity);
            return toDomain(saved);
        } catch (DataIntegrityViolationException ex) {
            if (violatesEmailUniqueConstraint(ex)) {
                throw new DuplicateEmailException(user.email());
            }
            throw ex;
        }
    }

    @Override
    public Optional<User> findById(UUID id) {
        return userJpaRepository.findById(id).map(this::toDomain);
    }

    private boolean violatesEmailUniqueConstraint(DataIntegrityViolationException ex) {
        Throwable cause = ex.getCause();
        while (cause != null) {
            if (cause instanceof ConstraintViolationException constraintViolation) {
                String constraintName = constraintViolation.getConstraintName();
                return constraintName != null
                        && constraintName.toLowerCase(Locale.ROOT).contains(EMAIL_UNIQUE_CONSTRAINT);
            }
            cause = cause.getCause();
        }
        return false;
    }

    private User toDomain(UserEntity entity) {
        return new User(entity.getId(), entity.getName(), entity.getEmail());
    }
}
