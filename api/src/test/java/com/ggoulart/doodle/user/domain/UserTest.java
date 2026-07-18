package com.ggoulart.doodle.user.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class UserTest {

    @Test
    void trimsNameAndNormalizesEmailToLowerCase() {
        User user = new User(UUID.randomUUID(), "  Ada Lovelace  ", "  Ada@Example.com  ");

        assertThat(user.name()).isEqualTo("Ada Lovelace");
        assertThat(user.email()).isEqualTo("ada@example.com");
    }

    @Test
    void throwsWhenNameIsBlank() {
        assertThatThrownBy(() -> new User(UUID.randomUUID(), "   ", "ada@example.com"))
                .isInstanceOf(InvalidNameException.class);
    }

    @Test
    void throwsWhenNameIsNull() {
        assertThatThrownBy(() -> new User(UUID.randomUUID(), null, "ada@example.com"))
                .isInstanceOf(InvalidNameException.class);
    }

    @Test
    void throwsWhenEmailIsNotValid() {
        assertThatThrownBy(() -> new User(UUID.randomUUID(), "Ada Lovelace", "not-an-email"))
                .isInstanceOf(InvalidEmailException.class);
    }

    @Test
    void throwsWhenEmailIsNull() {
        assertThatThrownBy(() -> new User(UUID.randomUUID(), "Ada Lovelace", null))
                .isInstanceOf(InvalidEmailException.class);
    }
}
