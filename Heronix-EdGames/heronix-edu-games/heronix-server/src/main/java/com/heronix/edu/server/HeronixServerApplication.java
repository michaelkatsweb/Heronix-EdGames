package com.heronix.edu.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main Spring Boot application class for Heronix Educational Games Server.
 *
 * This server handles:
 * - Device registration and management
 * - Student and class management
 * - Game score synchronization
 * - Teacher dashboard and reporting
 * - FERPA-compliant audit logging
 */
@SpringBootApplication
@EnableJpaRepositories
@EnableScheduling
public class HeronixServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(HeronixServerApplication.class, args);
    }
}
