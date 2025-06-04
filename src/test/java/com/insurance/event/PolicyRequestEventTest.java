package com.insurance.event;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.insurance.domain.enums.PolicyRequestStatus;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PolicyRequestEventTest {

    private UUID policyRequestId;
    private UUID customerId;
    private PolicyRequestStatus status;
    private PolicyRequestEvent event;

    private static class TestPolicyRequestEvent extends PolicyRequestEvent {
        public TestPolicyRequestEvent(UUID policyRequestId, UUID customerId, PolicyRequestStatus status) {
            super(policyRequestId, customerId, status);
        }
    }

    @BeforeEach
    void setUp() {
        policyRequestId = UUID.randomUUID();
        customerId = UUID.randomUUID();
        status = PolicyRequestStatus.RECEIVED;
        event = new TestPolicyRequestEvent(policyRequestId, customerId, status);
    }

    @Test
    void testEventCreation() {
        assertEquals(policyRequestId, event.getPolicyRequestId());
        assertEquals(customerId, event.getCustomerId());
        assertEquals(status, event.getStatus());
    }

    @Test
    void testTimestampGeneration() {
        LocalDateTime timestamp = event.getTimestamp();
        assertNotNull(timestamp);
        assertTrue(timestamp.isBefore(LocalDateTime.now().plusSeconds(1)));
        assertTrue(timestamp.isAfter(LocalDateTime.now().minusSeconds(1)));
    }

    @Test
    void testEventType() {
        assertEquals("TestPolicyRequestEvent", event.getEventType());
    }

    @Test
    void testEventWithDifferentStatus() {
        PolicyRequestEvent differentEvent = new TestPolicyRequestEvent(
            policyRequestId, 
            customerId, 
            PolicyRequestStatus.VALIDATED
        );

        assertEquals(PolicyRequestStatus.VALIDATED, differentEvent.getStatus());
        assertEquals(policyRequestId, differentEvent.getPolicyRequestId());
        assertEquals(customerId, differentEvent.getCustomerId());
    }

    @Test
    void testEventWithDifferentIds() {
        UUID differentPolicyId = UUID.randomUUID();
        UUID differentCustomerId = UUID.randomUUID();

        PolicyRequestEvent differentEvent = new TestPolicyRequestEvent(
            differentPolicyId,
            differentCustomerId,
            status
        );

        assertEquals(differentPolicyId, differentEvent.getPolicyRequestId());
        assertEquals(differentCustomerId, differentEvent.getCustomerId());
        assertEquals(status, differentEvent.getStatus());
    }

    @Test
    void testRequiredFields() {
        assertNotNull(event.getPolicyRequestId(), "PolicyRequestId should not be null");
        assertNotNull(event.getCustomerId(), "CustomerId should not be null");
        assertNotNull(event.getStatus(), "Status should not be null");
        assertNotNull(event.getTimestamp(), "Timestamp should not be null");
        assertNotNull(event.getEventType(), "EventType should not be null");
    }
} 