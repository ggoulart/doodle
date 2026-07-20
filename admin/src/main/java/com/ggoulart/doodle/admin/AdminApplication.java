package com.ggoulart.doodle.admin;

import de.codecentric.boot.admin.server.config.EnableAdminServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@EnableAdminServer
@SpringBootApplication
public class AdminApplication {

    static void main(String[] args) {
        SpringApplication.run(AdminApplication.class, args);
    }
}
