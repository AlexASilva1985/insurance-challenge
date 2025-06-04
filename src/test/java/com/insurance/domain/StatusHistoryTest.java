package com.insurance.domain;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.insurance.domain.enums.PolicyRequestStatus;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class StatusHistoryTest {

    private StatusHistory statusHistory;
    private UUID policyRequestId;

    @BeforeEach
    void setUp() {
        policyRequestId = UUID.randomUUID();
        statusHistory = new StatusHistory();
        statusHistory.setPolicyRequestId(policyRequestId);
        statusHistory.setPreviousStatus(PolicyRequestStatus.RECEIVED);
        statusHistory.setNewStatus(PolicyRequestStatus.VALIDATED);
        statusHistory.setChangedAt(LocalDateTime.now());
    }

    @Test
    void testCreateStatusHistoryWithCorrectData() {
        assertNotNull(statusHistory);
        assertEquals(policyRequestId, statusHistory.getPolicyRequestId());
        assertEquals(PolicyRequestStatus.RECEIVED, statusHistory.getPreviousStatus());
        assertEquals(PolicyRequestStatus.VALIDATED, statusHistory.getNewStatus());
        assertNotNull(statusHistory.getChangedAt());
    }

    @Test
    void testValidateRequiredFields() {
        StatusHistory invalidHistory = new StatusHistory();
        
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            invalidHistory.validate();
        });
        assertTrue(exception.getMessage().contains("required"));

        invalidHistory.setPolicyRequestId(policyRequestId);
        invalidHistory.setNewStatus(PolicyRequestStatus.VALIDATED);
        
        exception = assertThrows(IllegalArgumentException.class, () -> {
            invalidHistory.validate();
        });
        assertTrue(exception.getMessage().contains("previousStatus"));
    }

    @Test
    void testNotAllowSameStatusValues() {
        assertThrows(IllegalArgumentException.class, () -> {
            statusHistory.setNewStatus(statusHistory.getPreviousStatus());
        });
    }

    @Test
    void testValidateStatusTransitions() {
        // Transição válida
        assertDoesNotThrow(() -> {
            statusHistory.setPreviousStatus(PolicyRequestStatus.VALIDATED);
            statusHistory.setNewStatus(PolicyRequestStatus.PENDING);
        });

        // Transição inválida
        Exception exception = assertThrows(IllegalStateException.class, () -> {
            statusHistory.setPreviousStatus(PolicyRequestStatus.REJECTED);
            statusHistory.setNewStatus(PolicyRequestStatus.PENDING);
        });
        assertTrue(exception.getMessage().contains("Invalid status transition"));
    }

    @Test
    void testAutomaticallySetCreatedAtOnNew() {
        StatusHistory newHistory = new StatusHistory();
        newHistory.setPolicyRequestId(UUID.randomUUID());
        newHistory.setPreviousStatus(PolicyRequestStatus.RECEIVED);
        newHistory.setNewStatus(PolicyRequestStatus.VALIDATED);
        
        newHistory.onCreate();
        
        assertNotNull(newHistory.getChangedAt());
        assertTrue(newHistory.getChangedAt().isBefore(LocalDateTime.now().plusSeconds(1)));
        assertTrue(newHistory.getChangedAt().isAfter(LocalDateTime.now().minusSeconds(1)));
    }
} 