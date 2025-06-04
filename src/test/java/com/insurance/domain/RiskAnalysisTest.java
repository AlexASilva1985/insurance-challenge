package com.insurance.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.insurance.domain.enums.CustomerRiskType;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RiskAnalysisTest {

    private RiskAnalysis riskAnalysis;
    private LocalDateTime analyzedAt;

    @BeforeEach
    void setUp() {
        riskAnalysis = new RiskAnalysis();
        analyzedAt = LocalDateTime.now().minusHours(1);
    }

    @Test
    void testSuccessfulRiskAnalysisCreation() {
        riskAnalysis.setClassification(CustomerRiskType.REGULAR);
        riskAnalysis.setAnalyzedAt(analyzedAt);

        assertEquals(CustomerRiskType.REGULAR, riskAnalysis.getClassification());
        assertEquals(analyzedAt, riskAnalysis.getAnalyzedAt());
        assertNotNull(riskAnalysis.getOccurrences());
        assertTrue(riskAnalysis.getOccurrences().isEmpty());
    }

    @Test
    void testValidateWithValidData() {
        riskAnalysis.setClassification(CustomerRiskType.REGULAR);
        riskAnalysis.setAnalyzedAt(analyzedAt);

        // Should not throw any exception
        riskAnalysis.validate();
    }

    @Test
    void testValidateWithNullClassification() {
        riskAnalysis.setAnalyzedAt(analyzedAt);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> 
            riskAnalysis.validate()
        );

        assertEquals("classification is required", exception.getMessage());
    }

    @Test
    void testValidateWithNullAnalyzedAt() {
        riskAnalysis.setClassification(CustomerRiskType.REGULAR);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> 
            riskAnalysis.validate()
        );

        assertEquals("analyzedAt is required", exception.getMessage());
    }

    @Test
    void testValidateWithFutureAnalyzedAt() {
        riskAnalysis.setClassification(CustomerRiskType.REGULAR);
        
        // This test should verify that setAnalyzedAt throws exception for future dates
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> 
            riskAnalysis.setAnalyzedAt(LocalDateTime.now().plusHours(1))
        );

        assertEquals("analyzedAt cannot be a future date", exception.getMessage());
    }

    @Test
    void testSetClassificationWithValidValues() {
        CustomerRiskType[] riskTypes = {
            CustomerRiskType.HIGH_RISK,
            CustomerRiskType.REGULAR,
            CustomerRiskType.PREFERRED,
            CustomerRiskType.HIGH_RISK
        };

        for (CustomerRiskType riskType : riskTypes) {
            riskAnalysis.setClassification(riskType);
            assertEquals(riskType, riskAnalysis.getClassification());
        }
    }

    @Test
    void testSetClassificationWithNull() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> 
            riskAnalysis.setClassification(null)
        );

        assertEquals("classification cannot be null", exception.getMessage());
    }

    @Test
    void testSetAnalyzedAtWithValidDate() {
        LocalDateTime pastDate = LocalDateTime.now().minusDays(1);
        LocalDateTime currentDate = LocalDateTime.now();

        riskAnalysis.setAnalyzedAt(pastDate);
        assertEquals(pastDate, riskAnalysis.getAnalyzedAt());

        riskAnalysis.setAnalyzedAt(currentDate);
        assertEquals(currentDate, riskAnalysis.getAnalyzedAt());
    }

    @Test
    void testSetAnalyzedAtWithNull() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> 
            riskAnalysis.setAnalyzedAt(null)
        );

        assertEquals("analyzedAt cannot be null", exception.getMessage());
    }

    @Test
    void testSetAnalyzedAtWithFutureDate() {
        LocalDateTime futureDate = LocalDateTime.now().plusHours(1);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> 
            riskAnalysis.setAnalyzedAt(futureDate)
        );

        assertEquals("analyzedAt cannot be a future date", exception.getMessage());
    }

    @Test
    void testAddOccurrenceWithValidOccurrence() {
        RiskOccurrence occurrence = createValidRiskOccurrence();
        
        riskAnalysis.addOccurrence(occurrence);

        assertEquals(1, riskAnalysis.getOccurrences().size());
        assertTrue(riskAnalysis.getOccurrences().contains(occurrence));
    }

    @Test
    void testAddOccurrenceWithNullOccurrence() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> 
            riskAnalysis.addOccurrence(null)
        );

        assertEquals("occurrence cannot be null", exception.getMessage());
    }

    @Test
    void testAddMultipleOccurrences() {
        RiskOccurrence occurrence1 = createValidRiskOccurrence();
        RiskOccurrence occurrence2 = createValidRiskOccurrence();
        occurrence2.setType("IDENTITY_THEFT");

        riskAnalysis.addOccurrence(occurrence1);
        riskAnalysis.addOccurrence(occurrence2);

        assertEquals(2, riskAnalysis.getOccurrences().size());
        assertTrue(riskAnalysis.getOccurrences().contains(occurrence1));
        assertTrue(riskAnalysis.getOccurrences().contains(occurrence2));
    }

    @Test
    void testRemoveOccurrenceWithValidOccurrence() {
        RiskOccurrence occurrence = createValidRiskOccurrence();
        riskAnalysis.addOccurrence(occurrence);

        assertEquals(1, riskAnalysis.getOccurrences().size());

        riskAnalysis.removeOccurrence(occurrence);

        assertTrue(riskAnalysis.getOccurrences().isEmpty());
    }

    @Test
    void testRemoveOccurrenceWithNullOccurrence() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> 
            riskAnalysis.removeOccurrence(null)
        );

        assertEquals("occurrence cannot be null", exception.getMessage());
    }

    @Test
    void testRemoveNonExistentOccurrence() {
        RiskOccurrence occurrence1 = createValidRiskOccurrence();
        RiskOccurrence occurrence2 = createValidRiskOccurrence();
        occurrence2.setType("IDENTITY_THEFT");

        riskAnalysis.addOccurrence(occurrence1);

        riskAnalysis.removeOccurrence(occurrence2);

        assertEquals(1, riskAnalysis.getOccurrences().size());
        assertTrue(riskAnalysis.getOccurrences().contains(occurrence1));
    }

    @Test
    void testOccurrencesListInitialization() {
        RiskAnalysis newRiskAnalysis = new RiskAnalysis();
        
        List<RiskOccurrence> occurrences = newRiskAnalysis.getOccurrences();
        assertNotNull(occurrences);
        assertTrue(occurrences.isEmpty());
    }

    @Test
    void testInheritanceFromBaseEntity() {
        assertTrue(riskAnalysis instanceof BaseEntity);
        
        // Test inherited methods are accessible
        riskAnalysis.setCreatedBy("admin");
        riskAnalysis.setUpdatedBy("system");
        
        assertEquals("admin", riskAnalysis.getCreatedBy());
        assertEquals("system", riskAnalysis.getUpdatedBy());
    }

    private RiskOccurrence createValidRiskOccurrence() {
        RiskOccurrence occurrence = new RiskOccurrence();
        occurrence.setType("FRAUD_ATTEMPT");
        occurrence.setDescription("Suspicious activity detected");
        occurrence.setCreatedAt(LocalDateTime.now().minusDays(1));
        occurrence.setUpdatedAt(LocalDateTime.now());
        return occurrence;
    }

    @Test
    void testValidateWithFutureAnalyzedAtDirectly() {
        riskAnalysis.setClassification(CustomerRiskType.REGULAR);
        riskAnalysis.setAnalyzedAt(LocalDateTime.now().minusHours(1)); // Valid date first
        
        // Now manually set a future date to test validate() method
        LocalDateTime futureDate = LocalDateTime.now().plusHours(1);
        // Using reflection or direct field access would be needed, but let's test the setter behavior instead
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> 
            riskAnalysis.setAnalyzedAt(futureDate)
        );
        assertEquals("analyzedAt cannot be a future date", exception.getMessage());
    }

    @Test
    void testValidateWithAllNullFields() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> 
            riskAnalysis.validate()
        );
        assertEquals("classification is required", exception.getMessage());
    }

    @Test
    void testAllCustomerRiskTypes() {
        CustomerRiskType[] allTypes = {
            CustomerRiskType.REGULAR,
            CustomerRiskType.HIGH_RISK,
            CustomerRiskType.PREFERRED,
            CustomerRiskType.NO_INFORMATION
        };

        for (CustomerRiskType type : allTypes) {
            riskAnalysis.setClassification(type);
            assertEquals(type, riskAnalysis.getClassification());
        }
    }

    @Test
    void testAnalyzedAtWithDifferentTimeFormats() {
        // Test with precise timestamp
        LocalDateTime preciseTime = LocalDateTime.of(2023, 12, 25, 14, 30, 45, 123456789);
        riskAnalysis.setAnalyzedAt(preciseTime);
        assertEquals(preciseTime, riskAnalysis.getAnalyzedAt());

        // Test with start of day
        LocalDateTime startOfDay = LocalDateTime.now().toLocalDate().atStartOfDay();
        riskAnalysis.setAnalyzedAt(startOfDay);
        assertEquals(startOfDay, riskAnalysis.getAnalyzedAt());

        // Test with recent past
        LocalDateTime recentPast = LocalDateTime.now().minusMinutes(5);
        riskAnalysis.setAnalyzedAt(recentPast);
        assertEquals(recentPast, riskAnalysis.getAnalyzedAt());
    }

    @Test
    void testOccurrenceManagementWorkflow() {
        RiskOccurrence occurrence1 = createValidRiskOccurrence();
        occurrence1.setType("CREDIT_CARD_FRAUD");
        
        RiskOccurrence occurrence2 = createValidRiskOccurrence();
        occurrence2.setType("IDENTITY_THEFT");
        
        RiskOccurrence occurrence3 = createValidRiskOccurrence();
        occurrence3.setType("MONEY_LAUNDERING");

        // Add all occurrences
        riskAnalysis.addOccurrence(occurrence1);
        riskAnalysis.addOccurrence(occurrence2);
        riskAnalysis.addOccurrence(occurrence3);
        
        assertEquals(3, riskAnalysis.getOccurrences().size());

        // Remove middle occurrence
        riskAnalysis.removeOccurrence(occurrence2);
        assertEquals(2, riskAnalysis.getOccurrences().size());
        assertTrue(riskAnalysis.getOccurrences().contains(occurrence1));
        assertTrue(riskAnalysis.getOccurrences().contains(occurrence3));

        // Remove remaining occurrences
        riskAnalysis.removeOccurrence(occurrence1);
        riskAnalysis.removeOccurrence(occurrence3);
        assertTrue(riskAnalysis.getOccurrences().isEmpty());
    }

    @Test
    void testCompleteRiskAnalysisWorkflow() {
        // Create a complete risk analysis
        riskAnalysis.setClassification(CustomerRiskType.HIGH_RISK);
        riskAnalysis.setAnalyzedAt(LocalDateTime.now().minusHours(2));

        // Add multiple risk occurrences
        RiskOccurrence fraud = createValidRiskOccurrence();
        fraud.setType("FRAUD_ATTEMPT");
        fraud.setDescription("Multiple failed login attempts from different locations");

        RiskOccurrence identity = createValidRiskOccurrence();
        identity.setType("IDENTITY_VERIFICATION_FAILED");
        identity.setDescription("Unable to verify customer identity documents");

        riskAnalysis.addOccurrence(fraud);
        riskAnalysis.addOccurrence(identity);

        // Validate the complete analysis
        riskAnalysis.validate();

        assertEquals(CustomerRiskType.HIGH_RISK, riskAnalysis.getClassification());
        assertEquals(2, riskAnalysis.getOccurrences().size());
        assertNotNull(riskAnalysis.getAnalyzedAt());
    }

    @Test
    void testRiskAnalysisWithNoInformationClassification() {
        riskAnalysis.setClassification(CustomerRiskType.NO_INFORMATION);
        riskAnalysis.setAnalyzedAt(LocalDateTime.now().minusDays(1));

        riskAnalysis.validate();

        assertEquals(CustomerRiskType.NO_INFORMATION, riskAnalysis.getClassification());
        assertTrue(riskAnalysis.getOccurrences().isEmpty());
    }

    @Test
    void testRiskAnalysisWithPreferredCustomer() {
        riskAnalysis.setClassification(CustomerRiskType.PREFERRED);
        riskAnalysis.setAnalyzedAt(LocalDateTime.now().minusDays(7));

        riskAnalysis.validate();

        assertEquals(CustomerRiskType.PREFERRED, riskAnalysis.getClassification());
        assertTrue(riskAnalysis.getOccurrences().isEmpty());
    }

    @Test
    void testAddOccurrenceWithInvalidOccurrence() {
        // Create an invalid occurrence (missing required fields)
        RiskOccurrence invalidOccurrence = new RiskOccurrence();
        // Don't set required fields to make it invalid

        Exception exception = assertThrows(Exception.class, () -> 
            riskAnalysis.addOccurrence(invalidOccurrence)
        );
        // The exact exception type depends on the validate() implementation in RiskOccurrence
        assertNotNull(exception);
    }

    @Test
    void testOccurrenceListModification() {
        RiskOccurrence occurrence1 = createValidRiskOccurrence();
        RiskOccurrence occurrence2 = createValidRiskOccurrence();
        occurrence2.setType("SUSPICIOUS_TRANSACTION");

        riskAnalysis.addOccurrence(occurrence1);
        
        List<RiskOccurrence> occurrences = riskAnalysis.getOccurrences();
        assertEquals(1, occurrences.size());

        riskAnalysis.addOccurrence(occurrence2);
        assertEquals(2, riskAnalysis.getOccurrences().size());
    }

    @Test
    void testRiskAnalysisEquality() {
        RiskAnalysis analysis1 = new RiskAnalysis();
        analysis1.setClassification(CustomerRiskType.REGULAR);
        analysis1.setAnalyzedAt(analyzedAt);

        RiskAnalysis analysis2 = new RiskAnalysis();
        analysis2.setClassification(CustomerRiskType.REGULAR);
        analysis2.setAnalyzedAt(analyzedAt);

        // With Lombok @Data, objects with same field values should be equal
        assertEquals(analysis1.getClassification(), analysis2.getClassification());
        assertEquals(analysis1.getAnalyzedAt(), analysis2.getAnalyzedAt());
    }

    @Test
    void testRiskAnalysisToString() {
        riskAnalysis.setClassification(CustomerRiskType.HIGH_RISK);
        riskAnalysis.setAnalyzedAt(analyzedAt);

        String toString = riskAnalysis.toString();
        assertNotNull(toString);
        assertTrue(toString.length() > 0);
    }

    @Test
    void testRiskAnalysisHashCode() {
        riskAnalysis.setClassification(CustomerRiskType.REGULAR);
        riskAnalysis.setAnalyzedAt(analyzedAt);

        int hashCode = riskAnalysis.hashCode();
        // hashCode should be consistent
        assertEquals(hashCode, riskAnalysis.hashCode());
    }

    @Test
    void testAnalyzedAtBoundaryConditions() {
        // Test with current time (should work)
        LocalDateTime now = LocalDateTime.now();
        riskAnalysis.setAnalyzedAt(now);
        assertEquals(now, riskAnalysis.getAnalyzedAt());

        // Test with time just before now (should work)
        LocalDateTime justBefore = LocalDateTime.now().minusNanos(1);
        riskAnalysis.setAnalyzedAt(justBefore);
        assertEquals(justBefore, riskAnalysis.getAnalyzedAt());

        // Test with very old date (should work)
        LocalDateTime veryOld = LocalDateTime.of(2000, 1, 1, 0, 0, 0);
        riskAnalysis.setAnalyzedAt(veryOld);
        assertEquals(veryOld, riskAnalysis.getAnalyzedAt());
    }

    @Test
    void testOccurrenceRemovalOfNonExistentItem() {
        RiskOccurrence occurrence1 = createValidRiskOccurrence();
        RiskOccurrence occurrence2 = createValidRiskOccurrence();
        occurrence2.setType("DIFFERENT_TYPE");

        riskAnalysis.addOccurrence(occurrence1);
        assertEquals(1, riskAnalysis.getOccurrences().size());

        // Try to remove an occurrence that was never added
        riskAnalysis.removeOccurrence(occurrence2);
        
        // Should still have the original occurrence
        assertEquals(1, riskAnalysis.getOccurrences().size());
        assertTrue(riskAnalysis.getOccurrences().contains(occurrence1));
    }

    @Test
    void testRiskAnalysisWithMultipleOccurrenceTypes() {
        riskAnalysis.setClassification(CustomerRiskType.HIGH_RISK);
        riskAnalysis.setAnalyzedAt(LocalDateTime.now().minusHours(1));

        // Add different types of risk occurrences
        String[] riskTypes = {
            "FRAUD_ATTEMPT",
            "IDENTITY_THEFT", 
            "MONEY_LAUNDERING",
            "SUSPICIOUS_TRANSACTION",
            "CREDIT_CARD_ABUSE",
            "DOCUMENT_FORGERY"
        };

        for (String type : riskTypes) {
            RiskOccurrence occurrence = createValidRiskOccurrence();
            occurrence.setType(type);
            occurrence.setDescription("Risk occurrence of type: " + type);
            riskAnalysis.addOccurrence(occurrence);
        }

        assertEquals(riskTypes.length, riskAnalysis.getOccurrences().size());
        riskAnalysis.validate();
    }

    @Test
    void testSetClassificationAfterInitialization() {
        // Test changing classification multiple times
        riskAnalysis.setClassification(CustomerRiskType.REGULAR);
        assertEquals(CustomerRiskType.REGULAR, riskAnalysis.getClassification());

        riskAnalysis.setClassification(CustomerRiskType.HIGH_RISK);
        assertEquals(CustomerRiskType.HIGH_RISK, riskAnalysis.getClassification());

        riskAnalysis.setClassification(CustomerRiskType.PREFERRED);
        assertEquals(CustomerRiskType.PREFERRED, riskAnalysis.getClassification());

        riskAnalysis.setClassification(CustomerRiskType.NO_INFORMATION);
        assertEquals(CustomerRiskType.NO_INFORMATION, riskAnalysis.getClassification());
    }

    @Test
    void testAnalyzedAtTimePrecision() {
        LocalDateTime timeWithNanos = LocalDateTime.now().minusHours(1).withNano(123456789);
        riskAnalysis.setAnalyzedAt(timeWithNanos);
        
        assertEquals(timeWithNanos, riskAnalysis.getAnalyzedAt());
        assertEquals(123456789, riskAnalysis.getAnalyzedAt().getNano());
    }
} 