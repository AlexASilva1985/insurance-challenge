package com.insurance.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.insurance.domain.PolicyRequest;
import com.insurance.domain.RiskAnalysis;
import com.insurance.domain.RiskOccurrence;
import com.insurance.domain.enums.CustomerRiskType;
import com.insurance.infrastructure.client.FraudAnalysisClient;
import com.insurance.infrastructure.client.dto.FraudAnalysisResponse;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FraudAnalysisServiceImplTest {

    @Mock
    private FraudAnalysisClient fraudAnalysisClient;

    @InjectMocks
    private FraudAnalysisServiceImpl fraudAnalysisService;

    private PolicyRequest policyRequest;
    private UUID requestId;
    private UUID customerId;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        requestId = UUID.randomUUID();
        customerId = UUID.randomUUID();
        now = LocalDateTime.now();

        policyRequest = new PolicyRequest();
        policyRequest.setId(requestId);
        policyRequest.setCustomerId(customerId);
    }

    @Test
    void testAnalyzeFraudWithNoOccurrences() {
        FraudAnalysisResponse response = createFraudAnalysisResponse(
            CustomerRiskType.REGULAR,
            now,
            List.of()
        );

        when(fraudAnalysisClient.analyzeFraud(eq(requestId), eq(customerId)))
            .thenReturn(response);

        RiskAnalysis result = fraudAnalysisService.analyzeFraud(policyRequest);

        assertNotNull(result);
        assertEquals(CustomerRiskType.REGULAR, result.getClassification());
        assertEquals(now, result.getAnalyzedAt());
        assertTrue(result.getOccurrences().isEmpty());

        verify(fraudAnalysisClient).analyzeFraud(requestId, customerId);
    }

    @Test
    void testAnalyzeFraudWithMultipleOccurrences() {

        List<FraudAnalysisResponse.RiskOccurrenceResponse> occurrences = Arrays.asList(
            createRiskOccurrenceResponse(
                "CLAIMS_FREQUENCY",
                "High frequency of claims in the last 12 months",
                now.minusDays(5),
                now.minusDays(4)
            ),
            createRiskOccurrenceResponse(
                "PAYMENT_ISSUES",
                "Multiple payment defaults detected",
                now.minusDays(3),
                now.minusDays(2)
            )
        );

        FraudAnalysisResponse response = createFraudAnalysisResponse(
            CustomerRiskType.HIGH_RISK,
            now,
            occurrences
        );

        when(fraudAnalysisClient.analyzeFraud(eq(requestId), eq(customerId)))
            .thenReturn(response);

        RiskAnalysis result = fraudAnalysisService.analyzeFraud(policyRequest);

        assertNotNull(result);
        assertEquals(CustomerRiskType.HIGH_RISK, result.getClassification());
        assertEquals(now, result.getAnalyzedAt());
        assertEquals(2, result.getOccurrences().size());

        List<RiskOccurrence> resultOccurrences = result.getOccurrences();
        
        RiskOccurrence firstOccurrence = resultOccurrences.get(0);
        assertEquals("CLAIMS_FREQUENCY", firstOccurrence.getType());
        assertEquals("High frequency of claims in the last 12 months", firstOccurrence.getDescription());
        assertEquals(now.minusDays(5), firstOccurrence.getCreatedAt());
        assertEquals(now.minusDays(4), firstOccurrence.getUpdatedAt());

        RiskOccurrence secondOccurrence = resultOccurrences.get(1);
        assertEquals("PAYMENT_ISSUES", secondOccurrence.getType());
        assertEquals("Multiple payment defaults detected", secondOccurrence.getDescription());
        assertEquals(now.minusDays(3), secondOccurrence.getCreatedAt());
        assertEquals(now.minusDays(2), secondOccurrence.getUpdatedAt());

        verify(fraudAnalysisClient).analyzeFraud(requestId, customerId);
    }

    @Test
    void testAnalyzeFraudWithPreferredCustomer() {

        FraudAnalysisResponse response = createFraudAnalysisResponse(
            CustomerRiskType.PREFERRED,
            now,
            List.of()
        );

        when(fraudAnalysisClient.analyzeFraud(eq(requestId), eq(customerId)))
            .thenReturn(response);

        RiskAnalysis result = fraudAnalysisService.analyzeFraud(policyRequest);

        assertNotNull(result);
        assertEquals(CustomerRiskType.PREFERRED, result.getClassification());
        assertEquals(now, result.getAnalyzedAt());
        assertTrue(result.getOccurrences().isEmpty());

        verify(fraudAnalysisClient).analyzeFraud(requestId, customerId);
    }

    private FraudAnalysisResponse createFraudAnalysisResponse(
            CustomerRiskType classification,
            LocalDateTime analyzedAt,
            List<FraudAnalysisResponse.RiskOccurrenceResponse> occurrences) {
        FraudAnalysisResponse response = new FraudAnalysisResponse();
        response.setClassification(classification);
        response.setAnalyzedAt(analyzedAt);
        response.setOccurrences(occurrences);
        return response;
    }

    private FraudAnalysisResponse.RiskOccurrenceResponse createRiskOccurrenceResponse(
            String type,
            String description,
            LocalDateTime createdAt,
            LocalDateTime updatedAt) {
        FraudAnalysisResponse.RiskOccurrenceResponse response = new FraudAnalysisResponse.RiskOccurrenceResponse();
        response.setType(type);
        response.setDescription(description);
        response.setCreatedAt(createdAt);
        response.setUpdatedAt(updatedAt);
        return response;
    }
} 