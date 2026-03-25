package com.notifier;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * The execution of the Spring Boot application is initiated from this class.
 * Auto-configuration and component scanning are enabled via the @SpringBootApplication annotation.
 */
@SpringBootApplication
public class MultiChannelNotifierApplication {

    public static void main(String[] args) {
        SpringApplication.run(MultiChannelNotifierApplication.class, args);
    }
}