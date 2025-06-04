package com.insurance.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RiskOccurrenceTest {

    private RiskOccurrence riskOccurrence;

    @BeforeEach
    void setUp() {
        riskOccurrence = new RiskOccurrence();
    }

    @Test
    void testSuccessfulRiskOccurrenceCreation() {
        LocalDateTime now = LocalDateTime.now().minusMinutes(1);
        
        riskOccurrence.setType("FRAUD");
        riskOccurrence.setDescription("Suspicious activity detected");
        riskOccurrence.setCreatedAt(now);
        riskOccurrence.setUpdatedAt(now);

        assertEquals("FRAUD", riskOccurrence.getType());
        assertEquals("Suspicious activity detected", riskOccurrence.getDescription());
        assertEquals(now, riskOccurrence.getCreatedAt());
        assertEquals(now, riskOccurrence.getUpdatedAt());
    }

    @Test
    void testSetTypeWithValidValue() {
        riskOccurrence.setType("IDENTITY_THEFT");
        assertEquals("IDENTITY_THEFT", riskOccurrence.getType());
    }

    @Test
    void testSetTypeWithNull() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> 
            riskOccurrence.setType(null)
        );
        assertEquals("type cannot be empty", exception.getMessage());
    }

    @Test
    void testSetTypeWithEmptyString() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> 
            riskOccurrence.setType("")
        );
        assertEquals("type cannot be empty", exception.getMessage());
    }

    @Test
    void testSetTypeWithWhitespaceOnly() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> 
            riskOccurrence.setType("   ")
        );
        assertEquals("type cannot be empty", exception.getMessage());
    }

    @Test
    void testSetDescriptionWithValidValue() {
        String description = "Detailed description of the risk occurrence";
        riskOccurrence.setDescription(description);
        assertEquals(description, riskOccurrence.getDescription());
    }

    @Test
    void testSetDescriptionWithNull() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> 
            riskOccurrence.setDescription(null)
        );
        assertEquals("description cannot be empty", exception.getMessage());
    }

    @Test
    void testSetDescriptionWithEmptyString() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> 
            riskOccurrence.setDescription("")
        );
        assertEquals("description cannot be empty", exception.getMessage());
    }

    @Test
    void testSetDescriptionWithWhitespaceOnly() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> 
            riskOccurrence.setDescription("   ")
        );
        assertEquals("description cannot be empty", exception.getMessage());
    }

    @Test
    void testSetCreatedAtWithValidDate() {
        LocalDateTime pastDate = LocalDateTime.now().minusDays(1);
        riskOccurrence.setCreatedAt(pastDate);
        assertEquals(pastDate, riskOccurrence.getCreatedAt());
    }

    @Test
    void testSetCreatedAtWithCurrentDate() {
        LocalDateTime currentDate = LocalDateTime.now();
        riskOccurrence.setCreatedAt(currentDate);
        assertEquals(currentDate, riskOccurrence.getCreatedAt());
    }

    @Test
    void testSetCreatedAtWithNull() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> 
            riskOccurrence.setCreatedAt(null)
        );
        assertEquals("createdAt cannot be null", exception.getMessage());
    }

    @Test
    void testSetCreatedAtWithFutureDate() {
        LocalDateTime futureDate = LocalDateTime.now().plusHours(1);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> 
            riskOccurrence.setCreatedAt(futureDate)
        );
        assertEquals("createdAt cannot be a future date", exception.getMessage());
    }

    @Test
    void testSetUpdatedAtWithValidDate() {
        LocalDateTime pastDate = LocalDateTime.now().minusDays(1);
        LocalDateTime laterDate = LocalDateTime.now().minusHours(1);
        
        riskOccurrence.setCreatedAt(pastDate);
        riskOccurrence.setUpdatedAt(laterDate);
        
        assertEquals(laterDate, riskOccurrence.getUpdatedAt());
    }

    @Test
    void testSetUpdatedAtWithNull() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> 
            riskOccurrence.setUpdatedAt(null)
        );
        assertEquals("updatedAt cannot be null", exception.getMessage());
    }

    @Test
    void testSetUpdatedAtWithFutureDate() {
        LocalDateTime futureDate = LocalDateTime.now().plusHours(1);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> 
            riskOccurrence.setUpdatedAt(futureDate)
        );
        assertEquals("updatedAt cannot be a future date", exception.getMessage());
    }

    @Test
    void testSetUpdatedAtBeforeCreatedAt() {
        LocalDateTime createdAt = LocalDateTime.now().minusHours(1);
        LocalDateTime updatedAt = LocalDateTime.now().minusHours(2);
        
        riskOccurrence.setCreatedAt(createdAt);
        
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> 
            riskOccurrence.setUpdatedAt(updatedAt)
        );
        assertEquals("updatedAt cannot be before createdAt", exception.getMessage());
    }

    @Test
    void testValidateWithValidData() {
        LocalDateTime now = LocalDateTime.now().minusMinutes(1);
        
        riskOccurrence.setType("FRAUD");
        riskOccurrence.setDescription("Test description");
        riskOccurrence.setCreatedAt(now);
        riskOccurrence.setUpdatedAt(now);

        // Should not throw any exception
        riskOccurrence.validate();
    }

    @Test
    void testValidateWithNullType() {
        LocalDateTime now = LocalDateTime.now().minusMinutes(1);
        
        riskOccurrence.setDescription("Test description");
        riskOccurrence.setCreatedAt(now);
        riskOccurrence.setUpdatedAt(now);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> 
            riskOccurrence.validate()
        );
        assertEquals("type is required", exception.getMessage());
    }

    @Test
    void testValidateWithNullDescription() {
        LocalDateTime now = LocalDateTime.now().minusMinutes(1);
        
        riskOccurrence.setType("FRAUD");
        riskOccurrence.setCreatedAt(now);
        riskOccurrence.setUpdatedAt(now);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> 
            riskOccurrence.validate()
        );
        assertEquals("description is required", exception.getMessage());
    }

    @Test
    void testValidateWithNullCreatedAt() {
        LocalDateTime now = LocalDateTime.now().minusMinutes(1);
        
        riskOccurrence.setType("FRAUD");
        riskOccurrence.setDescription("Test description");
        riskOccurrence.setUpdatedAt(now);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> 
            riskOccurrence.validate()
        );
        assertEquals("createdAt is required", exception.getMessage());
    }

    @Test
    void testValidateWithNullUpdatedAt() {
        LocalDateTime now = LocalDateTime.now().minusMinutes(1);
        
        riskOccurrence.setType("FRAUD");
        riskOccurrence.setDescription("Test description");
        riskOccurrence.setCreatedAt(now);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> 
            riskOccurrence.validate()
        );
        assertEquals("updatedAt is required", exception.getMessage());
    }

    @Test
    void testOnCreate() {
        riskOccurrence.onCreate();
        
        assertNotNull(riskOccurrence.getCreatedAt());
        assertNotNull(riskOccurrence.getUpdatedAt());
        assertEquals(riskOccurrence.getCreatedAt(), riskOccurrence.getUpdatedAt());
    }

    @Test
    void testOnUpdate() {
        LocalDateTime oldUpdatedAt = LocalDateTime.now().minusHours(1);
        riskOccurrence.setCreatedAt(oldUpdatedAt);
        riskOccurrence.setUpdatedAt(oldUpdatedAt);
        
        // Wait a bit to ensure time difference
        try {
            Thread.sleep(1);
        } catch (InterruptedException e) {
            // Ignore
        }
        
        riskOccurrence.onUpdate();
        
        assertTrue(riskOccurrence.getUpdatedAt().isAfter(oldUpdatedAt));
    }

    @Test
    void testInheritanceFromBaseEntity() {
        assertTrue(riskOccurrence instanceof BaseEntity);
        
        // Test inherited methods are accessible
        riskOccurrence.setCreatedBy("admin");
        riskOccurrence.setUpdatedBy("system");
        
        assertEquals("admin", riskOccurrence.getCreatedBy());
        assertEquals("system", riskOccurrence.getUpdatedBy());
    }

    @Test
    void testToString() {
        riskOccurrence.setType("FRAUD");
        riskOccurrence.setDescription("Test description");
        
        String toString = riskOccurrence.toString();
        assertNotNull(toString);
        assertTrue(toString.length() > 0);
    }
} 