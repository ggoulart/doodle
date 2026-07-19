package com.ggoulart.doodle.user.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ggoulart.doodle.user.application.DuplicateEmailException;
import com.ggoulart.doodle.user.domain.User;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

@ExtendWith(MockitoExtension.class)
class JpaUserRepositoryTest {

    @Mock
    private UserJpaRepository userJpaRepository;

    @Test
    void saveMapsDomainUserToEntityAndBackToDomain() {
        JpaUserRepository repository = new JpaUserRepository(userJpaRepository);
        User user = new User(UUID.randomUUID(), "Ada Lovelace", "ada@example.com");
        when(userJpaRepository.saveAndFlush(any(UserEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User saved = repository.save(user);

        assertThat(saved).isEqualTo(user);
        ArgumentCaptor<UserEntity> captor = ArgumentCaptor.forClass(UserEntity.class);
        verify(userJpaRepository).saveAndFlush(captor.capture());
        assertThat(captor.getValue().getId()).isEqualTo(user.id());
        assertThat(captor.getValue().getName()).isEqualTo(user.name());
        assertThat(captor.getValue().getEmail()).isEqualTo(user.email());
    }

    @Test
    void saveThrowsDuplicateEmailExceptionWhenEmailConstraintViolated() {
        JpaUserRepository repository = new JpaUserRepository(userJpaRepository);
        User user = new User(UUID.randomUUID(), "Ada Lovelace", "ada@example.com");
        DataIntegrityViolationException exception = wrapConstraintViolation("uk_users_email");
        when(userJpaRepository.saveAndFlush(any(UserEntity.class))).thenThrow(exception);

        assertThatThrownBy(() -> repository.save(user)).isInstanceOf(DuplicateEmailException.class);
    }

    @Test
    void saveRethrowsWhenViolatedConstraintIsNotTheEmailOne() {
        JpaUserRepository repository = new JpaUserRepository(userJpaRepository);
        User user = new User(UUID.randomUUID(), "Ada Lovelace", "ada@example.com");
        DataIntegrityViolationException exception = wrapConstraintViolation("some_other_constraint");
        when(userJpaRepository.saveAndFlush(any(UserEntity.class))).thenThrow(exception);

        assertThatThrownBy(() -> repository.save(user)).isSameAs(exception);
    }

    private DataIntegrityViolationException wrapConstraintViolation(String constraintName) {
        ConstraintViolationException constraintViolationException = new ConstraintViolationException(
                "could not execute statement", new SQLException("constraint violated"), constraintName);
        return new DataIntegrityViolationException("could not execute statement", constraintViolationException);
    }

    @Test
    void findByIdMapsEntityToDomainWhenPresent() {
        JpaUserRepository repository = new JpaUserRepository(userJpaRepository);
        UUID id = UUID.randomUUID();
        UserEntity entity = new UserEntity(id, "Ada Lovelace", "ada@example.com");
        when(userJpaRepository.findById(id)).thenReturn(Optional.of(entity));

        Optional<User> found = repository.findById(id);

        assertThat(found).contains(new User(id, "Ada Lovelace", "ada@example.com"));
    }

    @Test
    void findByIdReturnsEmptyWhenMissing() {
        JpaUserRepository repository = new JpaUserRepository(userJpaRepository);
        UUID id = UUID.randomUUID();
        when(userJpaRepository.findById(id)).thenReturn(Optional.empty());

        Optional<User> found = repository.findById(id);

        assertThat(found).isEmpty();
    }
}
