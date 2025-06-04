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
        occurrence.setType("FRAUD");
        occurrence.setDescription("Suspicious activity detected");
        occurrence.setCreatedAt(LocalDateTime.now().minusMinutes(5));
        occurrence.setUpdatedAt(LocalDateTime.now().minusMinutes(5));
        return occurrence;
    }
} 