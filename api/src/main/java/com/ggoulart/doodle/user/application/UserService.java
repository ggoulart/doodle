package com.ggoulart.doodle.user.application;

import com.ggoulart.doodle.calendar.application.CreateCalendarUseCase;
import com.ggoulart.doodle.user.domain.User;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class UserService implements CreateUserUseCase, GetUserUseCase {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    private final UserRepository userRepository;
    private final CreateCalendarUseCase createCalendarUseCase;

    UserService(UserRepository userRepository, CreateCalendarUseCase createCalendarUseCase) {
        this.userRepository = userRepository;
        this.createCalendarUseCase = createCalendarUseCase;
    }

    @Transactional
    @Override
    public User createUser(CreateUserCommand command) {
        String name = command.name() == null ? "" : command.name().trim();
        String email = command.email() == null ? "" : command.email().trim().toLowerCase(Locale.ROOT);

        if (name.isEmpty()) {
            throw new InvalidNameException("name must not be empty");
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new InvalidEmailException("email is not valid: " + email);
        }
        if (userRepository.existsByEmail(email)) {
            throw new DuplicateEmailException(email);
        }

        User user = new User(UUID.randomUUID(), name, email);
        User savedUser = userRepository.save(user);
        createCalendarUseCase.createCalendar(savedUser.id());
        return savedUser;
    }

    @Override
    public Optional<User> getUser(UUID id) {
        return userRepository.findById(id);
    }
}
