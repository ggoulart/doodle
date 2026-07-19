package com.ggoulart.doodle.user.persistence;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.UUID;

@Entity
@Table(name = "users", uniqueConstraints = @UniqueConstraint(name = "uk_users_email", columnNames = "email"))
class UserEntity {

    @Id
    private UUID id;

    private String name;

    private String email;

    protected UserEntity() {
    }

    UserEntity(UUID id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
    }

    UUID getId() {
        return id;
    }

    String getName() {
        return name;
    }

    String getEmail() {
        return email;
    }
}
