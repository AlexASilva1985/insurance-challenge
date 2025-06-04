package com.insurance.domain.enums;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class PolicyRequestStatusTest {

    @Test
    void testAllStatusValuesExist() {
        PolicyRequestStatus[] statuses = PolicyRequestStatus.values();
        assertEquals(6, statuses.length);
    }

    @Test
    void testSpecificStatusValues() {
        assertEquals("RECEIVED", PolicyRequestStatus.RECEIVED.name());
        assertEquals("VALIDATED", PolicyRequestStatus.VALIDATED.name());
        assertEquals("PENDING", PolicyRequestStatus.PENDING.name());
        assertEquals("APPROVED", PolicyRequestStatus.APPROVED.name());
        assertEquals("REJECTED", PolicyRequestStatus.REJECTED.name());
        assertEquals("CANCELLED", PolicyRequestStatus.CANCELLED.name());
    }

    @Test
    void testValueOf() {
        assertEquals(PolicyRequestStatus.RECEIVED, PolicyRequestStatus.valueOf("RECEIVED"));
        assertEquals(PolicyRequestStatus.VALIDATED, PolicyRequestStatus.valueOf("VALIDATED"));
        assertEquals(PolicyRequestStatus.PENDING, PolicyRequestStatus.valueOf("PENDING"));
        assertEquals(PolicyRequestStatus.APPROVED, PolicyRequestStatus.valueOf("APPROVED"));
        assertEquals(PolicyRequestStatus.REJECTED, PolicyRequestStatus.valueOf("REJECTED"));
        assertEquals(PolicyRequestStatus.CANCELLED, PolicyRequestStatus.valueOf("CANCELLED"));
    }

    @Test
    void testEnumOrder() {
        PolicyRequestStatus[] statuses = PolicyRequestStatus.values();
        assertEquals(PolicyRequestStatus.RECEIVED, statuses[0]);
        assertEquals(PolicyRequestStatus.VALIDATED, statuses[1]);
        assertEquals(PolicyRequestStatus.PENDING, statuses[2]);
        assertEquals(PolicyRequestStatus.APPROVED, statuses[3]);
        assertEquals(PolicyRequestStatus.REJECTED, statuses[4]);
        assertEquals(PolicyRequestStatus.CANCELLED, statuses[5]);
    }

    @Test
    void testToString() {
        for (PolicyRequestStatus status : PolicyRequestStatus.values()) {
            assertNotNull(status.toString());
            assertTrue(status.toString().length() > 0);
        }
    }

    @Test
    void testEquality() {
        assertEquals(PolicyRequestStatus.RECEIVED, PolicyRequestStatus.RECEIVED);
        assertEquals(PolicyRequestStatus.APPROVED, PolicyRequestStatus.APPROVED);
    }

    @Test
    void testHashCode() {
        for (PolicyRequestStatus status : PolicyRequestStatus.values()) {
            assertNotNull(status.hashCode());
        }
    }

    @Test
    void testOrdinalValues() {
        assertEquals(0, PolicyRequestStatus.RECEIVED.ordinal());
        assertEquals(1, PolicyRequestStatus.VALIDATED.ordinal());
        assertEquals(2, PolicyRequestStatus.PENDING.ordinal());
        assertEquals(3, PolicyRequestStatus.APPROVED.ordinal());
        assertEquals(4, PolicyRequestStatus.REJECTED.ordinal());
        assertEquals(5, PolicyRequestStatus.CANCELLED.ordinal());
    }
} 