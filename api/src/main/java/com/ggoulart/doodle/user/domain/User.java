package com.ggoulart.doodle.user.domain;

import java.util.Locale;
import java.util.UUID;
import java.util.regex.Pattern;

public record User(UUID id, String name, String email) {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    public User {
        name = name == null ? "" : name.trim();
        email = email == null ? "" : email.trim().toLowerCase(Locale.ROOT);

        if (name.isEmpty()) {
            throw new InvalidNameException("name must not be empty");
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new InvalidEmailException("email is not valid: " + email);
        }
    }
}
