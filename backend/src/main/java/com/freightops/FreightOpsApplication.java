package com.freightops;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * FreightOps - Freight Management System
 * Main Spring Boot Application Class
 */
@SpringBootApplication
@EnableJpaAuditing
public class FreightOpsApplication {

    public static void main(String[] args) {
        SpringApplication.run(FreightOpsApplication.class, args);
    }
}
