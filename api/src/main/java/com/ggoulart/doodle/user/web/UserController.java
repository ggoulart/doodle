package com.ggoulart.doodle.user.web;

import com.ggoulart.doodle.user.application.CreateUserCommand;
import com.ggoulart.doodle.user.application.CreateUserUseCase;
import com.ggoulart.doodle.user.domain.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
public class UserController {

    private final CreateUserUseCase createUserUseCase;

    UserController(CreateUserUseCase createUserUseCase) {
        this.createUserUseCase = createUserUseCase;
    }

    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody CreateUserRequest request) {
        CreateUserCommand command = new CreateUserCommand(request.name(), request.email());
        User user = createUserUseCase.createUser(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }
}
