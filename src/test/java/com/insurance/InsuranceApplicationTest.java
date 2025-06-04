package com.insurance;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

class InsuranceApplicationTest {

    @Test
    void applicationClassExists() {
        assertNotNull(InsuranceApplication.class);
    }

    @Test
    void mainMethodExists() throws NoSuchMethodException {
        assertNotNull(InsuranceApplication.class.getMethod("main", String[].class));
    }
} 