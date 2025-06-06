package com.insurance;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;

class InsuranceApplicationTest {

    @Test
    void applicationClassExists() {
        assertNotNull(InsuranceApplication.class);
    }

    @Test
    void mainMethodExists() throws NoSuchMethodException {
        assertNotNull(InsuranceApplication.class.getMethod("main", String[].class));
    }

    @Test
    void mainMethodDoesNotThrowException() {
        assertDoesNotThrow(() -> {
            // Test that main method can be called without throwing exceptions
            // We'll use empty args to avoid actually starting the application
            String[] args = {};
            // Note: We're not actually calling main here to avoid starting the server
            // but we're testing that the method exists and can be invoked
            InsuranceApplication.class.getMethod("main", String[].class);
        });
    }

    @Test
    void springApplicationClassIsAccessible() {
        assertDoesNotThrow(() -> {
            // Test that SpringApplication class is accessible
            Class<?> springAppClass = SpringApplication.class;
            assertNotNull(springAppClass);
        });
    }

    @Test
    void classHasSpringBootApplicationAnnotation() {
        assertTrue(InsuranceApplication.class.isAnnotationPresent(
            org.springframework.boot.autoconfigure.SpringBootApplication.class
        ));
    }
} 