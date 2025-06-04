package com.insurance.event;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.insurance.domain.PolicyRequest;
import com.insurance.domain.enums.PolicyRequestStatus;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PolicyRequestCreatedEventTest {

    private PolicyRequest mockPolicyRequest;
    private UUID policyRequestId;
    private UUID customerId;
    private PolicyRequestCreatedEvent event;

    @BeforeEach
    void setUp() {
        policyRequestId = UUID.randomUUID();
        customerId = UUID.randomUUID();
        
        mockPolicyRequest = mock(PolicyRequest.class);
        when(mockPolicyRequest.getId()).thenReturn(policyRequestId);
        when(mockPolicyRequest.getCustomerId()).thenReturn(customerId);
        
        event = new PolicyRequestCreatedEvent(mockPolicyRequest);
    }

    @Test
    void testEventCreation() {
        assertEquals(policyRequestId, event.getPolicyRequestId());
        assertEquals(customerId, event.getCustomerId());
        assertEquals(PolicyRequestStatus.RECEIVED, event.getStatus());
        assertNotNull(event.getTimestamp());
        assertTrue(event.getTimestamp().isBefore(LocalDateTime.now().plusSeconds(1)));
        assertTrue(event.getTimestamp().isAfter(LocalDateTime.now().minusSeconds(1)));
    }

    @Test
    void testEventType() {
        assertEquals("PolicyRequestCreatedEvent", event.getEventType());
    }

    @Test
    void testInheritedFields() {
        // Verifica se os campos herdados da classe pai estão corretos
        assertEquals(policyRequestId, event.getPolicyRequestId());
        assertEquals(customerId, event.getCustomerId());
        assertEquals(PolicyRequestStatus.RECEIVED, event.getStatus());
        assertNotNull(event.getTimestamp());
    }
} 