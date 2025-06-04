package com.insurance.domain;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.insurance.domain.enums.InsuranceCategory;
import com.insurance.domain.enums.PaymentMethod;
import com.insurance.domain.enums.PolicyRequestStatus;
import com.insurance.domain.enums.SalesChannel;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PolicyRequestTest {

    private PolicyRequest policyRequest;
    private UUID customerId;
    private UUID productId;
    private Map<String, BigDecimal> coverages;

    @BeforeEach
    void setUp() {
        customerId = UUID.randomUUID();
        productId = UUID.randomUUID();

        coverages = new HashMap<>();
        coverages.put("COLLISION", new BigDecimal("30000.00"));
        coverages.put("THEFT", new BigDecimal("20000.00"));

        policyRequest = new PolicyRequest();
        policyRequest.setId(UUID.randomUUID());
        policyRequest.setCustomerId(customerId);
        policyRequest.setProductId(productId);
        policyRequest.setCategory(InsuranceCategory.AUTO);
        policyRequest.setSalesChannel(SalesChannel.BROKER);
        policyRequest.setPaymentMethod(PaymentMethod.CREDIT_CARD);
        policyRequest.setTotalMonthlyPremiumAmount(new BigDecimal("500.00"));
        policyRequest.setInsuredAmount(new BigDecimal("50000.00"));
        policyRequest.setCoverages(coverages);
        policyRequest.setAssistances(List.of("Roadside Assistance", "Glass Protection"));
        policyRequest.setStatus(PolicyRequestStatus.RECEIVED);
    }

    @Test
    void testCreatePolicyRequestWithCorrectData() {
        assertNotNull(policyRequest.getId());
        assertEquals(customerId, policyRequest.getCustomerId());
        assertEquals(productId, policyRequest.getProductId());
        assertEquals(InsuranceCategory.AUTO, policyRequest.getCategory());
        assertEquals(SalesChannel.BROKER, policyRequest.getSalesChannel());
        assertEquals(PaymentMethod.CREDIT_CARD, policyRequest.getPaymentMethod());
        assertEquals(new BigDecimal("500.00"), policyRequest.getTotalMonthlyPremiumAmount());
        assertEquals(new BigDecimal("50000.00"), policyRequest.getInsuredAmount());
        assertEquals(2, policyRequest.getCoverages().size());
        assertEquals(2, policyRequest.getAssistances().size());
        assertEquals(PolicyRequestStatus.RECEIVED, policyRequest.getStatus());
        assertTrue(policyRequest.getStatusHistory().isEmpty());
    }

    @Test
    void testUpdateStatusAndCreateStatusHistory() {
        policyRequest.updateStatus(PolicyRequestStatus.VALIDATED);
        
        assertEquals(PolicyRequestStatus.VALIDATED, policyRequest.getStatus());
        assertEquals(1, policyRequest.getStatusHistory().size());
        
        StatusHistory lastHistory = policyRequest.getStatusHistory().get(0);
        assertEquals(PolicyRequestStatus.RECEIVED, lastHistory.getPreviousStatus());
        assertEquals(PolicyRequestStatus.VALIDATED, lastHistory.getNewStatus());
        assertNotNull(lastHistory.getChangedAt());
        assertTrue(lastHistory.getChangedAt().isBefore(LocalDateTime.now().plusSeconds(1)));
    }

    @Test
    void testAddRiskAnalysis() {
        RiskAnalysis riskAnalysis = new RiskAnalysis();
        riskAnalysis.setId(UUID.randomUUID());
        riskAnalysis.setAnalyzedAt(LocalDateTime.now());

        policyRequest.setRiskAnalysis(riskAnalysis);

        assertNotNull(policyRequest.getRiskAnalysis());
        assertEquals(riskAnalysis.getId(), policyRequest.getRiskAnalysis().getId());
    }

    @Test
    void testValidateRequiredFields() {
        PolicyRequest invalidRequest = new PolicyRequest();
        
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            invalidRequest.validate();
        });
        assertTrue(exception.getMessage().contains("customerId"));

        invalidRequest.setCustomerId(UUID.randomUUID());
        invalidRequest.setProductId(UUID.randomUUID());
        
        exception = assertThrows(IllegalArgumentException.class, () -> {
            invalidRequest.validate();
        });
        assertTrue(exception.getMessage().contains("category"));
    }

    @Test
    void testCalculateTotalCoverageAmount() {
        BigDecimal total = policyRequest.calculateTotalCoverageAmount();
        assertEquals(new BigDecimal("50000.00"), total);
    }

    @Test
    void testValidateStatusTransitions() {
        // Transição válida
        assertDoesNotThrow(() -> {
            policyRequest.updateStatus(PolicyRequestStatus.VALIDATED);
        });
        assertEquals(PolicyRequestStatus.VALIDATED, policyRequest.getStatus());

        // Transição inválida
        Exception exception = assertThrows(IllegalStateException.class, () -> {
            policyRequest.updateStatus(PolicyRequestStatus.RECEIVED);
        });
        assertTrue(exception.getMessage().contains("Invalid status transition"));
    }

    @Test
    void testNotAllowInvalidAmounts() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            policyRequest.setTotalMonthlyPremiumAmount(BigDecimal.ZERO);
        });
        assertTrue(exception.getMessage().contains("greater than zero"));

        exception = assertThrows(IllegalArgumentException.class, () -> {
            policyRequest.setInsuredAmount(new BigDecimal("-1.00"));
        });
        assertTrue(exception.getMessage().contains("greater than zero"));
    }
} 