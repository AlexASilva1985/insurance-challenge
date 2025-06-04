package com.insurance.domain;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import com.insurance.domain.enums.PolicyRequestStatus;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.List;
import java.util.ArrayList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class StatusHistoryTest {

    private StatusHistory statusHistory;
    private UUID policyRequestId;
    private PolicyRequest policyRequest;

    @BeforeEach
    void setUp() {
        policyRequestId = UUID.randomUUID();
        statusHistory = new StatusHistory();
        statusHistory.setPolicyRequestId(policyRequestId);
        statusHistory.setPreviousStatus(PolicyRequestStatus.RECEIVED);
        statusHistory.setNewStatus(PolicyRequestStatus.VALIDATED);
        statusHistory.setChangedAt(LocalDateTime.now());
        policyRequest = new PolicyRequest();
        policyRequest.setId(UUID.randomUUID());
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

    @Test
    void testStatusHistoryCreationWithAllFields() {
        StatusHistory history = new StatusHistory();
        history.setPolicyRequestId(policyRequest.getId());
        history.setPreviousStatus(PolicyRequestStatus.RECEIVED);
        history.setNewStatus(PolicyRequestStatus.VALIDATED);
        history.setChangedAt(LocalDateTime.now());
        history.setReason("Validation completed successfully");

        assertEquals(policyRequest.getId(), history.getPolicyRequestId());
        assertEquals(PolicyRequestStatus.RECEIVED, history.getPreviousStatus());
        assertEquals(PolicyRequestStatus.VALIDATED, history.getNewStatus());
        assertNotNull(history.getChangedAt());
        assertEquals("Validation completed successfully", history.getReason());
    }

    @Test
    void testStatusHistoryWithNullReason() {
        StatusHistory history = new StatusHistory();
        history.setPolicyRequestId(policyRequest.getId());
        history.setPreviousStatus(PolicyRequestStatus.VALIDATED);
        history.setNewStatus(PolicyRequestStatus.PENDING);
        history.setChangedAt(LocalDateTime.now());
        history.setReason(null);

        assertNull(history.getReason());
    }

    @Test
    void testStatusHistoryWithEmptyReason() {
        StatusHistory history = new StatusHistory();
        history.setPolicyRequestId(policyRequest.getId());
        history.setPreviousStatus(PolicyRequestStatus.PENDING);
        history.setNewStatus(PolicyRequestStatus.APPROVED);
        history.setChangedAt(LocalDateTime.now());
        history.setReason("");

        assertEquals("", history.getReason());
    }

    @Test
    void testStatusHistoryWithLongReason() {
        StatusHistory history = new StatusHistory();
        history.setPolicyRequestId(policyRequest.getId());
        history.setPreviousStatus(PolicyRequestStatus.RECEIVED);
        history.setNewStatus(PolicyRequestStatus.REJECTED);
        history.setChangedAt(LocalDateTime.now());
        
        String longReason = "This is a very long reason that explains in detail why the status was changed. " +
                           "It includes multiple sentences and provides comprehensive information about the decision process.";
        history.setReason(longReason);

        assertEquals(longReason, history.getReason());
    }

    @Test
    void testAllStatusTransitions() {
        LocalDateTime changeTime = LocalDateTime.now();

        // RECEIVED -> VALIDATED
        StatusHistory history1 = new StatusHistory();
        history1.setPolicyRequestId(policyRequest.getId());
        history1.setPreviousStatus(PolicyRequestStatus.RECEIVED);
        history1.setNewStatus(PolicyRequestStatus.VALIDATED);
        history1.setChangedAt(changeTime);
        history1.setReason("Document validation completed");

        // VALIDATED -> PENDING
        StatusHistory history2 = new StatusHistory();
        history2.setPolicyRequestId(policyRequest.getId());
        history2.setPreviousStatus(PolicyRequestStatus.VALIDATED);
        history2.setNewStatus(PolicyRequestStatus.PENDING);
        history2.setChangedAt(changeTime.plusMinutes(10));
        history2.setReason("Moved to pending for further review");

        // PENDING -> APPROVED
        StatusHistory history3 = new StatusHistory();
        history3.setPolicyRequestId(policyRequest.getId());
        history3.setPreviousStatus(PolicyRequestStatus.PENDING);
        history3.setNewStatus(PolicyRequestStatus.APPROVED);
        history3.setChangedAt(changeTime.plusMinutes(20));
        history3.setReason("All requirements met, approved");

        // Verify all transitions
        assertEquals(PolicyRequestStatus.VALIDATED, history1.getNewStatus());
        assertEquals(PolicyRequestStatus.PENDING, history2.getNewStatus());
        assertEquals(PolicyRequestStatus.APPROVED, history3.getNewStatus());
    }

    @Test
    void testRejectionTransitions() {
        LocalDateTime changeTime = LocalDateTime.now();

        // RECEIVED -> REJECTED
        StatusHistory rejectionFromReceived = new StatusHistory();
        rejectionFromReceived.setPolicyRequestId(policyRequest.getId());
        rejectionFromReceived.setPreviousStatus(PolicyRequestStatus.RECEIVED);
        rejectionFromReceived.setNewStatus(PolicyRequestStatus.REJECTED);
        rejectionFromReceived.setChangedAt(changeTime);
        rejectionFromReceived.setReason("Invalid documentation provided");

        // VALIDATED -> REJECTED
        StatusHistory rejectionFromValidated = new StatusHistory();
        rejectionFromValidated.setPolicyRequestId(policyRequest.getId());
        rejectionFromValidated.setPreviousStatus(PolicyRequestStatus.VALIDATED);
        rejectionFromValidated.setNewStatus(PolicyRequestStatus.REJECTED);
        rejectionFromValidated.setChangedAt(changeTime.plusMinutes(15));
        rejectionFromValidated.setReason("Risk assessment failed");

        // PENDING -> REJECTED
        StatusHistory rejectionFromPending = new StatusHistory();
        rejectionFromPending.setPolicyRequestId(policyRequest.getId());
        rejectionFromPending.setPreviousStatus(PolicyRequestStatus.PENDING);
        rejectionFromPending.setNewStatus(PolicyRequestStatus.REJECTED);
        rejectionFromPending.setChangedAt(changeTime.plusMinutes(30));
        rejectionFromPending.setReason("Credit check failed");

        // Verify all rejections
        assertEquals(PolicyRequestStatus.REJECTED, rejectionFromReceived.getNewStatus());
        assertEquals(PolicyRequestStatus.REJECTED, rejectionFromValidated.getNewStatus());
        assertEquals(PolicyRequestStatus.REJECTED, rejectionFromPending.getNewStatus());
    }

    @Test
    void testCancellationTransitions() {
        LocalDateTime changeTime = LocalDateTime.now();

        // RECEIVED -> CANCELLED
        StatusHistory cancellationFromReceived = new StatusHistory();
        cancellationFromReceived.setPolicyRequestId(policyRequest.getId());
        cancellationFromReceived.setPreviousStatus(PolicyRequestStatus.RECEIVED);
        cancellationFromReceived.setNewStatus(PolicyRequestStatus.CANCELLED);
        cancellationFromReceived.setChangedAt(changeTime);
        cancellationFromReceived.setReason("Customer requested cancellation");

        // VALIDATED -> CANCELLED
        StatusHistory cancellationFromValidated = new StatusHistory();
        cancellationFromValidated.setPolicyRequestId(policyRequest.getId());
        cancellationFromValidated.setPreviousStatus(PolicyRequestStatus.VALIDATED);
        cancellationFromValidated.setNewStatus(PolicyRequestStatus.CANCELLED);
        cancellationFromValidated.setChangedAt(changeTime.plusMinutes(10));
        cancellationFromValidated.setReason("System error, cancelled by admin");

        // Verify cancellations
        assertEquals(PolicyRequestStatus.CANCELLED, cancellationFromReceived.getNewStatus());
        assertEquals(PolicyRequestStatus.CANCELLED, cancellationFromValidated.getNewStatus());
    }

    @Test
    void testStatusHistoryWithDifferentPolicyRequests() {
        PolicyRequest anotherPolicyRequest = new PolicyRequest();
        anotherPolicyRequest.setId(UUID.randomUUID());

        StatusHistory history1 = new StatusHistory();
        history1.setPolicyRequestId(policyRequest.getId());
        history1.setPreviousStatus(PolicyRequestStatus.RECEIVED);
        history1.setNewStatus(PolicyRequestStatus.VALIDATED);
        history1.setChangedAt(LocalDateTime.now());

        StatusHistory history2 = new StatusHistory();
        history2.setPolicyRequestId(anotherPolicyRequest.getId());
        history2.setPreviousStatus(PolicyRequestStatus.RECEIVED);
        history2.setNewStatus(PolicyRequestStatus.VALIDATED);
        history2.setChangedAt(LocalDateTime.now());

        assertNotEquals(history1.getPolicyRequestId(), history2.getPolicyRequestId());
        assertEquals(history1.getPreviousStatus(), history2.getPreviousStatus());
        assertEquals(history1.getNewStatus(), history2.getNewStatus());
    }

    @Test
    void testStatusHistoryTimeOrder() {
        LocalDateTime baseTime = LocalDateTime.now();

        StatusHistory firstChange = new StatusHistory();
        firstChange.setChangedAt(baseTime);
        firstChange.setPreviousStatus(PolicyRequestStatus.RECEIVED);
        firstChange.setNewStatus(PolicyRequestStatus.VALIDATED);

        StatusHistory secondChange = new StatusHistory();
        secondChange.setChangedAt(baseTime.plusMinutes(5));
        secondChange.setPreviousStatus(PolicyRequestStatus.VALIDATED);
        secondChange.setNewStatus(PolicyRequestStatus.PENDING);

        StatusHistory thirdChange = new StatusHistory();
        thirdChange.setChangedAt(baseTime.plusMinutes(10));
        thirdChange.setPreviousStatus(PolicyRequestStatus.PENDING);
        thirdChange.setNewStatus(PolicyRequestStatus.APPROVED);

        // Verify time ordering
        assertTrue(firstChange.getChangedAt().isBefore(secondChange.getChangedAt()));
        assertTrue(secondChange.getChangedAt().isBefore(thirdChange.getChangedAt()));
    }

    @Test
    void testStatusHistoryWithSpecialCharactersInReason() {
        StatusHistory history = new StatusHistory();
        history.setPolicyRequestId(policyRequest.getId());
        history.setPreviousStatus(PolicyRequestStatus.RECEIVED);
        history.setNewStatus(PolicyRequestStatus.VALIDATED);
        history.setChangedAt(LocalDateTime.now());
        
        String specialReason = "Razão com acentos e çaracteres especiais: !@#$%^&*()";
        history.setReason(specialReason);

        assertEquals(specialReason, history.getReason());
    }

    @Test
    void testStatusHistoryEquality() {
        StatusHistory history1 = createStatusHistory();
        StatusHistory history2 = createStatusHistory();

        // With Lombok @Data, objects with same field values will be equal even with different IDs
        // So we'll test that objects with different data are not equal
        StatusHistory differentHistory = new StatusHistory();
        differentHistory.setPolicyRequestId(policyRequest.getId());
        differentHistory.setPreviousStatus(PolicyRequestStatus.VALIDATED); // Different from history1
        differentHistory.setNewStatus(PolicyRequestStatus.PENDING); // Different from history1
        differentHistory.setChangedAt(LocalDateTime.now());
        differentHistory.setReason("Different reason");

        assertNotEquals(history1, differentHistory);
        
        // Test that objects with same properties are equal (Lombok @Data behavior)
        assertEquals(history1, history2);
        assertEquals(history1.getPreviousStatus(), history2.getPreviousStatus());
        assertEquals(history1.getNewStatus(), history2.getNewStatus());
        assertEquals(history1.getReason(), history2.getReason());
    }

    @Test
    void testStatusHistoryToString() {
        StatusHistory history = createStatusHistory();
        String toString = history.toString();
        
        assertNotNull(toString);
        assertTrue(toString.length() > 0);
        // Since it extends BaseEntity with @Data, toString should include all fields
    }

    @Test
    void testStatusHistoryHashCode() {
        StatusHistory history = createStatusHistory();
        int hashCode = history.hashCode();
        
        // hashCode should be consistent
        assertEquals(hashCode, history.hashCode());
    }

    @Test
    void testInheritanceFromBaseEntity() {
        StatusHistory history = createStatusHistory();
        assertTrue(history instanceof BaseEntity);
        
        // Test inherited methods
        history.setCreatedBy("system");
        history.setUpdatedBy("admin");
        
        assertEquals("system", history.getCreatedBy());
        assertEquals("admin", history.getUpdatedBy());
    }

    @Test
    void testStatusHistoryWithPreciseTimestamp() {
        LocalDateTime preciseTime = LocalDateTime.of(2023, 12, 25, 14, 30, 45, 123456789);
        
        StatusHistory history = new StatusHistory();
        history.setPolicyRequestId(policyRequest.getId());
        history.setPreviousStatus(PolicyRequestStatus.VALIDATED);
        history.setNewStatus(PolicyRequestStatus.PENDING);
        history.setChangedAt(preciseTime);
        history.setReason("Precise timing test");

        assertEquals(preciseTime, history.getChangedAt());
        assertEquals(2023, history.getChangedAt().getYear());
        assertEquals(12, history.getChangedAt().getMonthValue());
        assertEquals(25, history.getChangedAt().getDayOfMonth());
        assertEquals(14, history.getChangedAt().getHour());
        assertEquals(30, history.getChangedAt().getMinute());
        assertEquals(45, history.getChangedAt().getSecond());
    }

    @Test
    void testStatusHistoryBatch() {
        // Test creating multiple status histories in sequence
        List<StatusHistory> histories = new ArrayList<>();
        PolicyRequestStatus[] statuses = {
            PolicyRequestStatus.RECEIVED,
            PolicyRequestStatus.VALIDATED,
            PolicyRequestStatus.PENDING,
            PolicyRequestStatus.APPROVED
        };

        for (int i = 0; i < statuses.length - 1; i++) {
            StatusHistory history = new StatusHistory();
            history.setPolicyRequestId(policyRequest.getId());
            history.setPreviousStatus(statuses[i]);
            history.setNewStatus(statuses[i + 1]);
            history.setChangedAt(LocalDateTime.now().plusMinutes(i * 5));
            history.setReason("Batch transition " + (i + 1));
            histories.add(history);
        }

        assertEquals(3, histories.size());
        
        // Verify the sequence
        assertEquals(PolicyRequestStatus.RECEIVED, histories.get(0).getPreviousStatus());
        assertEquals(PolicyRequestStatus.VALIDATED, histories.get(0).getNewStatus());
        
        assertEquals(PolicyRequestStatus.VALIDATED, histories.get(1).getPreviousStatus());
        assertEquals(PolicyRequestStatus.PENDING, histories.get(1).getNewStatus());
        
        assertEquals(PolicyRequestStatus.PENDING, histories.get(2).getPreviousStatus());
        assertEquals(PolicyRequestStatus.APPROVED, histories.get(2).getNewStatus());
    }

    @Test
    void testValidationWithMissingFields() {
        StatusHistory emptyHistory = new StatusHistory();

        // Test validation with missing policyRequestId
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            emptyHistory.validate();
        });
        assertEquals("policyRequestId is required", exception.getMessage());

        // Add policyRequestId and test missing previousStatus
        emptyHistory.setPolicyRequestId(UUID.randomUUID());
        exception = assertThrows(IllegalArgumentException.class, () -> {
            emptyHistory.validate();
        });
        assertEquals("previousStatus is required", exception.getMessage());

        // Add previousStatus and test missing newStatus
        emptyHistory.setPreviousStatus(PolicyRequestStatus.RECEIVED);
        exception = assertThrows(IllegalArgumentException.class, () -> {
            emptyHistory.validate();
        });
        assertEquals("newStatus is required", exception.getMessage());

        // Add newStatus and test missing changedAt
        emptyHistory.setNewStatus(PolicyRequestStatus.VALIDATED);
        exception = assertThrows(IllegalArgumentException.class, () -> {
            emptyHistory.validate();
        });
        assertEquals("changedAt is required", exception.getMessage());
    }

    @Test
    void testValidationWithAllRequiredFields() {
        StatusHistory validHistory = new StatusHistory();
        validHistory.setPolicyRequestId(UUID.randomUUID());
        validHistory.setPreviousStatus(PolicyRequestStatus.RECEIVED);
        validHistory.setNewStatus(PolicyRequestStatus.VALIDATED);
        validHistory.setChangedAt(LocalDateTime.now());

        // Should not throw any exception
        assertDoesNotThrow(() -> validHistory.validate());
    }

    @Test
    void testSetPolicyRequestIdValidation() {
        StatusHistory history = new StatusHistory();
        
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            history.setPolicyRequestId(null);
        });
        assertEquals("policyRequestId cannot be null", exception.getMessage());

        // Valid UUID should work
        UUID validId = UUID.randomUUID();
        history.setPolicyRequestId(validId);
        assertEquals(validId, history.getPolicyRequestId());
    }

    @Test
    void testSetPreviousStatusValidation() {
        StatusHistory history = new StatusHistory();
        
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            history.setPreviousStatus(null);
        });
        assertEquals("previousStatus cannot be null", exception.getMessage());

        // Valid status should work
        history.setPreviousStatus(PolicyRequestStatus.RECEIVED);
        assertEquals(PolicyRequestStatus.RECEIVED, history.getPreviousStatus());
    }

    @Test
    void testSetNewStatusValidation() {
        StatusHistory history = new StatusHistory();
        
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            history.setNewStatus(null);
        });
        assertEquals("newStatus cannot be null", exception.getMessage());

        // Test same status validation
        history.setPreviousStatus(PolicyRequestStatus.RECEIVED);
        exception = assertThrows(IllegalArgumentException.class, () -> {
            history.setNewStatus(PolicyRequestStatus.RECEIVED);
        });
        assertEquals("newStatus cannot be the same as previousStatus", exception.getMessage());
    }

    @Test
    void testInvalidStatusTransitions() {
        StatusHistory history = new StatusHistory();
        
        // Test invalid transition from RECEIVED to PENDING (should go through VALIDATED first)
        history.setPreviousStatus(PolicyRequestStatus.RECEIVED);
        Exception exception = assertThrows(IllegalStateException.class, () -> {
            history.setNewStatus(PolicyRequestStatus.PENDING);
        });
        assertTrue(exception.getMessage().contains("Invalid status transition"));

        // Test invalid transition from APPROVED (final state)
        history.setPreviousStatus(PolicyRequestStatus.APPROVED);
        exception = assertThrows(IllegalStateException.class, () -> {
            history.setNewStatus(PolicyRequestStatus.PENDING);
        });
        assertTrue(exception.getMessage().contains("Invalid status transition"));

        // Test invalid transition from REJECTED (final state)
        history.setPreviousStatus(PolicyRequestStatus.REJECTED);
        exception = assertThrows(IllegalStateException.class, () -> {
            history.setNewStatus(PolicyRequestStatus.VALIDATED);
        });
        assertTrue(exception.getMessage().contains("Invalid status transition"));

        // Test invalid transition from CANCELLED (final state)
        history.setPreviousStatus(PolicyRequestStatus.CANCELLED);
        exception = assertThrows(IllegalStateException.class, () -> {
            history.setNewStatus(PolicyRequestStatus.VALIDATED);
        });
        assertTrue(exception.getMessage().contains("Invalid status transition"));
    }

    @Test
    void testValidStatusTransitions() {
        StatusHistory history = new StatusHistory();
        
        // Test all valid transitions from RECEIVED
        history.setPreviousStatus(PolicyRequestStatus.RECEIVED);
        
        // RECEIVED -> VALIDATED
        history.setNewStatus(PolicyRequestStatus.VALIDATED);
        assertEquals(PolicyRequestStatus.VALIDATED, history.getNewStatus());
        
        // Reset and test RECEIVED -> REJECTED
        history.setPreviousStatus(PolicyRequestStatus.RECEIVED);
        history.setNewStatus(PolicyRequestStatus.REJECTED);
        assertEquals(PolicyRequestStatus.REJECTED, history.getNewStatus());
        
        // Reset and test RECEIVED -> CANCELLED
        history.setPreviousStatus(PolicyRequestStatus.RECEIVED);
        history.setNewStatus(PolicyRequestStatus.CANCELLED);
        assertEquals(PolicyRequestStatus.CANCELLED, history.getNewStatus());
    }

    @Test
    void testOnCreateSetsDefaultChangedAt() {
        StatusHistory newHistory = new StatusHistory();
        newHistory.onCreate();
        assertNotNull(newHistory.getChangedAt());
        assertTrue(newHistory.getChangedAt().isBefore(LocalDateTime.now().plusSeconds(1)));
    }

    @Test
    void testOnCreateDoesNotOverrideExistingChangedAt() {
        StatusHistory newHistory = new StatusHistory();
        LocalDateTime specificTime = LocalDateTime.of(2023, 1, 1, 12, 0, 0);
        newHistory.setChangedAt(specificTime);
        newHistory.onCreate();
        assertEquals(specificTime, newHistory.getChangedAt());
    }

    private StatusHistory createStatusHistory() {
        StatusHistory history = new StatusHistory();
        history.setPolicyRequestId(policyRequest.getId());
        history.setPreviousStatus(PolicyRequestStatus.RECEIVED);
        history.setNewStatus(PolicyRequestStatus.VALIDATED);
        history.setChangedAt(LocalDateTime.now());
        history.setReason("Test reason");
        return history;
    }
} 