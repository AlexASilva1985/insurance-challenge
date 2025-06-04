package com.insurance.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.insurance.domain.enums.PolicyRequestStatus;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class StatusHistoryDTOTest {

    private StatusHistoryDTO statusHistoryDTO;
    private LocalDateTime timestamp;

    @BeforeEach
    void setUp() {
        statusHistoryDTO = new StatusHistoryDTO();
        timestamp = LocalDateTime.now();
    }

    @Test
    void testGettersAndSetters() {
        statusHistoryDTO.setStatus(PolicyRequestStatus.RECEIVED);
        statusHistoryDTO.setTimestamp(timestamp);

        assertEquals(PolicyRequestStatus.RECEIVED, statusHistoryDTO.getStatus());
        assertEquals(timestamp, statusHistoryDTO.getTimestamp());
    }

    @Test
    void testAllStatuses() {
        PolicyRequestStatus[] statuses = {
            PolicyRequestStatus.RECEIVED,
            PolicyRequestStatus.VALIDATED,
            PolicyRequestStatus.PENDING,
            PolicyRequestStatus.APPROVED,
            PolicyRequestStatus.REJECTED,
            PolicyRequestStatus.CANCELLED
        };

        for (PolicyRequestStatus status : statuses) {
            statusHistoryDTO.setStatus(status);
            assertEquals(status, statusHistoryDTO.getStatus());
        }
    }

    @Test
    void testTimestampHandling() {
        LocalDateTime past = LocalDateTime.now().minusDays(1);
        LocalDateTime future = LocalDateTime.now().plusDays(1);

        statusHistoryDTO.setTimestamp(past);
        assertEquals(past, statusHistoryDTO.getTimestamp());

        statusHistoryDTO.setTimestamp(future);
        assertEquals(future, statusHistoryDTO.getTimestamp());

        statusHistoryDTO.setTimestamp(null);
        assertEquals(null, statusHistoryDTO.getTimestamp());
    }

    @Test
    void testEqualsAndHashCode() {
        StatusHistoryDTO dto1 = new StatusHistoryDTO();
        StatusHistoryDTO dto2 = new StatusHistoryDTO();

        dto1.setStatus(PolicyRequestStatus.VALIDATED);
        dto1.setTimestamp(timestamp);

        dto2.setStatus(PolicyRequestStatus.VALIDATED);
        dto2.setTimestamp(timestamp);

        assertEquals(dto1, dto2);
        assertEquals(dto1.hashCode(), dto2.hashCode());

        dto2.setStatus(PolicyRequestStatus.APPROVED);
        assertNotEquals(dto1, dto2);
    }

    @Test
    void testToString() {
        statusHistoryDTO.setStatus(PolicyRequestStatus.RECEIVED);
        statusHistoryDTO.setTimestamp(timestamp);

        String toString = statusHistoryDTO.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("StatusHistoryDTO"));
    }

    @Test
    void testNullValues() {
        StatusHistoryDTO dto = new StatusHistoryDTO();
        dto.setStatus(null);
        dto.setTimestamp(null);

        assertEquals(null, dto.getStatus());
        assertEquals(null, dto.getTimestamp());
    }

    @Test
    void testDefaultConstructor() {
        StatusHistoryDTO dto = new StatusHistoryDTO();
        assertEquals(null, dto.getStatus());
        assertEquals(null, dto.getTimestamp());
    }
} 