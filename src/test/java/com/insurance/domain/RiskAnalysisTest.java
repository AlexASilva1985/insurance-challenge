package com.insurance.domain;

import com.insurance.domain.enums.CustomerRiskType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class RiskAnalysisTest {

    private RiskAnalysis riskAnalysis;
    private RiskOccurrence occurrence;

    @BeforeEach
    void setUp() {
        riskAnalysis = new RiskAnalysis();
        riskAnalysis.setClassification(CustomerRiskType.REGULAR);
        riskAnalysis.setAnalyzedAt(LocalDateTime.now());
        
        occurrence = new RiskOccurrence();
        occurrence.setType("CREDIT_CHECK");
        occurrence.setDescription("Credit score below threshold");
        occurrence.setCreatedAt(LocalDateTime.now());
        occurrence.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    void shouldCreateRiskAnalysisWithCorrectData() {
        assertNotNull(riskAnalysis);
        assertEquals(CustomerRiskType.REGULAR, riskAnalysis.getClassification());
        assertNotNull(riskAnalysis.getAnalyzedAt());
        assertTrue(riskAnalysis.getOccurrences().isEmpty());
    }

    @Test
    void shouldManageOccurrences() {
        assertTrue(riskAnalysis.getOccurrences().isEmpty());
        
        riskAnalysis.addOccurrence(occurrence);
        assertEquals(1, riskAnalysis.getOccurrences().size());
        assertEquals("CREDIT_CHECK", riskAnalysis.getOccurrences().get(0).getType());
        
        riskAnalysis.removeOccurrence(occurrence);
        assertTrue(riskAnalysis.getOccurrences().isEmpty());
    }

    @Test
    void shouldValidateRequiredFields() {
        RiskAnalysis invalidAnalysis = new RiskAnalysis();
        
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            invalidAnalysis.validate();
        });
        assertTrue(exception.getMessage().contains("classification"));

        invalidAnalysis.setClassification(CustomerRiskType.HIGH_RISK);
        exception = assertThrows(IllegalArgumentException.class, () -> {
            invalidAnalysis.validate();
        });
        assertTrue(exception.getMessage().contains("analyzedAt"));
    }

    @Test
    void shouldNotAllowFutureAnalysisDate() {
        assertThrows(IllegalArgumentException.class, () -> {
            riskAnalysis.setAnalyzedAt(LocalDateTime.now().plusDays(1));
        });
    }

    @Test
    void shouldValidateOccurrences() {
        RiskOccurrence invalidOccurrence = new RiskOccurrence();
        
        assertThrows(IllegalArgumentException.class, () -> {
            riskAnalysis.addOccurrence(invalidOccurrence);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            riskAnalysis.addOccurrence(null);
        });
    }
} 