package com.ggoulart.doodle;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@OpenAPIDefinition(info = @Info(
        title = "Doodle API",
        description = "A mini Doodle: users manage time slots in a personal calendar and book them as meetings.",
        version = "v1"))
@SpringBootApplication
public class ApiApplication {

    static void main(String[] args) {
        SpringApplication.run(ApiApplication.class, args);
    }
}
