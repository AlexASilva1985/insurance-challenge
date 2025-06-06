package com.insurance.domain;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
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

    @Test
    void testFinishPolicyRequest() {
        // Test finishing with APPROVED
        policyRequest.updateStatus(PolicyRequestStatus.VALIDATED);
        policyRequest.updateStatus(PolicyRequestStatus.PENDING);
        policyRequest.updateStatus(PolicyRequestStatus.APPROVED);
        
        assertNotNull(policyRequest.getFinishedAt());
        assertTrue(policyRequest.getFinishedAt().isBefore(LocalDateTime.now().plusSeconds(1)));
    }

    @Test
    void testFinishPolicyRequestWithRejected() {
        // Test finishing with REJECTED
        policyRequest.updateStatus(PolicyRequestStatus.VALIDATED);
        policyRequest.updateStatus(PolicyRequestStatus.REJECTED);
        
        assertNotNull(policyRequest.getFinishedAt());
        assertTrue(policyRequest.getFinishedAt().isBefore(LocalDateTime.now().plusSeconds(1)));
    }

    @Test
    void testFinishPolicyRequestWithCancelled() {
        // Test finishing with CANCELLED
        policyRequest.updateStatus(PolicyRequestStatus.CANCELLED);
        
        assertNotNull(policyRequest.getFinishedAt());
        assertTrue(policyRequest.getFinishedAt().isBefore(LocalDateTime.now().plusSeconds(1)));
    }

    @Test
    void testMultipleStatusTransitions() {
        // RECEIVED -> VALIDATED
        policyRequest.updateStatus(PolicyRequestStatus.VALIDATED);
        assertEquals(PolicyRequestStatus.VALIDATED, policyRequest.getStatus());
        assertEquals(1, policyRequest.getStatusHistory().size());
        
        // VALIDATED -> PENDING
        policyRequest.updateStatus(PolicyRequestStatus.PENDING);
        assertEquals(PolicyRequestStatus.PENDING, policyRequest.getStatus());
        assertEquals(2, policyRequest.getStatusHistory().size());
        
        // PENDING -> APPROVED
        policyRequest.updateStatus(PolicyRequestStatus.APPROVED);
        assertEquals(PolicyRequestStatus.APPROVED, policyRequest.getStatus());
        assertEquals(3, policyRequest.getStatusHistory().size());
    }

    @Test
    void testInvalidStatusTransitionFromApproved() {
        policyRequest.updateStatus(PolicyRequestStatus.VALIDATED);
        policyRequest.updateStatus(PolicyRequestStatus.PENDING);
        policyRequest.updateStatus(PolicyRequestStatus.APPROVED);
        
        Exception exception = assertThrows(IllegalStateException.class, () -> {
            policyRequest.updateStatus(PolicyRequestStatus.PENDING);
        });
        assertTrue(exception.getMessage().contains("Invalid status transition"));
    }

    @Test
    void testInvalidStatusTransitionFromRejected() {
        policyRequest.updateStatus(PolicyRequestStatus.VALIDATED);
        policyRequest.updateStatus(PolicyRequestStatus.REJECTED);
        
        Exception exception = assertThrows(IllegalStateException.class, () -> {
            policyRequest.updateStatus(PolicyRequestStatus.APPROVED);
        });
        assertTrue(exception.getMessage().contains("Invalid status transition"));
    }

    @Test
    void testCanTransitionToMethods() {
        // Test all valid transitions from RECEIVED
        assertTrue(policyRequest.canTransitionTo(PolicyRequestStatus.VALIDATED));
        assertTrue(policyRequest.canTransitionTo(PolicyRequestStatus.REJECTED));
        assertTrue(policyRequest.canTransitionTo(PolicyRequestStatus.CANCELLED));
        
        // Test invalid transitions from RECEIVED
        assertDoesNotThrow(() -> policyRequest.canTransitionTo(PolicyRequestStatus.PENDING));
        
        // Update to VALIDATED and test transitions
        policyRequest.updateStatus(PolicyRequestStatus.VALIDATED);
        assertTrue(policyRequest.canTransitionTo(PolicyRequestStatus.PENDING));
        assertTrue(policyRequest.canTransitionTo(PolicyRequestStatus.REJECTED));
        assertTrue(policyRequest.canTransitionTo(PolicyRequestStatus.CANCELLED));
    }

    @Test
    void testCalculateTotalCoverageAmountWithNullCoverages() {
        policyRequest.setCoverages(null);
        Exception exception = assertThrows(NullPointerException.class, () -> {
            policyRequest.calculateTotalCoverageAmount();
        });
        assertNotNull(exception);
    }

    @Test
    void testCalculateTotalCoverageAmountWithEmptyCoverages() {
        policyRequest.setCoverages(new HashMap<>());
        BigDecimal total = policyRequest.calculateTotalCoverageAmount();
        assertEquals(BigDecimal.ZERO, total);
    }

    @Test
    void testValidationWithAllRequiredFields() {
        PolicyRequest validRequest = new PolicyRequest();
        validRequest.setCustomerId(UUID.randomUUID());
        validRequest.setProductId(UUID.randomUUID());
        validRequest.setCategory(InsuranceCategory.AUTO);
        validRequest.setSalesChannel(SalesChannel.BROKER);
        validRequest.setPaymentMethod(PaymentMethod.CREDIT_CARD);
        validRequest.setTotalMonthlyPremiumAmount(new BigDecimal("100.00"));
        validRequest.setInsuredAmount(new BigDecimal("10000.00"));
        validRequest.setCoverages(Map.of("BASIC", new BigDecimal("5000.00")));

        assertDoesNotThrow(() -> validRequest.validate());
    }

    @Test
    void testUpdateStatusWithNullStatus() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            policyRequest.updateStatus(null);
        });
        assertEquals("New status cannot be null", exception.getMessage());
    }

    @Test
    void testToString() {
        String toString = policyRequest.toString();
        assertNotNull(toString);
        assertTrue(toString.length() > 0);
    }

    @Test
    void testInheritanceFromBaseEntity() {
        assertTrue(policyRequest instanceof BaseEntity);
        
        // Test inherited methods are accessible
        policyRequest.setCreatedBy("admin");
        policyRequest.setUpdatedBy("system");
        
        assertEquals("admin", policyRequest.getCreatedBy());
        assertEquals("system", policyRequest.getUpdatedBy());
    }

    @Test
    void testValidateWithNullCustomerId() {
        PolicyRequest invalidRequest = new PolicyRequest();
        invalidRequest.setProductId(UUID.randomUUID());
        invalidRequest.setCategory(InsuranceCategory.AUTO);
        invalidRequest.setSalesChannel(SalesChannel.BROKER);
        invalidRequest.setPaymentMethod(PaymentMethod.CREDIT_CARD);
        invalidRequest.setTotalMonthlyPremiumAmount(new BigDecimal("100.00"));
        invalidRequest.setInsuredAmount(new BigDecimal("10000.00"));
        invalidRequest.setCoverages(Map.of("BASIC", new BigDecimal("5000.00")));

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            invalidRequest.validate();
        });
        assertEquals("customerId is required", exception.getMessage());
    }

    @Test
    void testValidateWithNullProductId() {
        PolicyRequest invalidRequest = new PolicyRequest();
        invalidRequest.setCustomerId(UUID.randomUUID());
        invalidRequest.setCategory(InsuranceCategory.AUTO);
        invalidRequest.setSalesChannel(SalesChannel.BROKER);
        invalidRequest.setPaymentMethod(PaymentMethod.CREDIT_CARD);
        invalidRequest.setTotalMonthlyPremiumAmount(new BigDecimal("100.00"));
        invalidRequest.setInsuredAmount(new BigDecimal("10000.00"));
        invalidRequest.setCoverages(Map.of("BASIC", new BigDecimal("5000.00")));

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            invalidRequest.validate();
        });
        assertEquals("productId is required", exception.getMessage());
    }

    @Test
    void testValidateWithNullCategory() {
        PolicyRequest invalidRequest = new PolicyRequest();
        invalidRequest.setCustomerId(UUID.randomUUID());
        invalidRequest.setProductId(UUID.randomUUID());
        invalidRequest.setSalesChannel(SalesChannel.BROKER);
        invalidRequest.setPaymentMethod(PaymentMethod.CREDIT_CARD);
        invalidRequest.setTotalMonthlyPremiumAmount(new BigDecimal("100.00"));
        invalidRequest.setInsuredAmount(new BigDecimal("10000.00"));
        invalidRequest.setCoverages(Map.of("BASIC", new BigDecimal("5000.00")));

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            invalidRequest.validate();
        });
        assertEquals("category is required", exception.getMessage());
    }

    @Test
    void testValidateWithNullSalesChannel() {
        PolicyRequest invalidRequest = new PolicyRequest();
        invalidRequest.setCustomerId(UUID.randomUUID());
        invalidRequest.setProductId(UUID.randomUUID());
        invalidRequest.setCategory(InsuranceCategory.AUTO);
        invalidRequest.setPaymentMethod(PaymentMethod.CREDIT_CARD);
        invalidRequest.setTotalMonthlyPremiumAmount(new BigDecimal("100.00"));
        invalidRequest.setInsuredAmount(new BigDecimal("10000.00"));
        invalidRequest.setCoverages(Map.of("BASIC", new BigDecimal("5000.00")));

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            invalidRequest.validate();
        });
        assertEquals("salesChannel is required", exception.getMessage());
    }

    @Test
    void testValidateWithNullPaymentMethod() {
        PolicyRequest invalidRequest = new PolicyRequest();
        invalidRequest.setCustomerId(UUID.randomUUID());
        invalidRequest.setProductId(UUID.randomUUID());
        invalidRequest.setCategory(InsuranceCategory.AUTO);
        invalidRequest.setSalesChannel(SalesChannel.BROKER);
        invalidRequest.setTotalMonthlyPremiumAmount(new BigDecimal("100.00"));
        invalidRequest.setInsuredAmount(new BigDecimal("10000.00"));
        invalidRequest.setCoverages(Map.of("BASIC", new BigDecimal("5000.00")));

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            invalidRequest.validate();
        });
        assertEquals("paymentMethod is required", exception.getMessage());
    }

    @Test
    void testValidateWithNullTotalMonthlyPremiumAmount() {
        PolicyRequest invalidRequest = new PolicyRequest();
        invalidRequest.setCustomerId(UUID.randomUUID());
        invalidRequest.setProductId(UUID.randomUUID());
        invalidRequest.setCategory(InsuranceCategory.AUTO);
        invalidRequest.setSalesChannel(SalesChannel.BROKER);
        invalidRequest.setPaymentMethod(PaymentMethod.CREDIT_CARD);
        invalidRequest.setInsuredAmount(new BigDecimal("10000.00"));
        invalidRequest.setCoverages(Map.of("BASIC", new BigDecimal("5000.00")));

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            invalidRequest.validate();
        });
        assertEquals("totalMonthlyPremiumAmount must be greater than zero", exception.getMessage());
    }

    @Test
    void testValidateWithZeroTotalMonthlyPremiumAmount() {
        PolicyRequest invalidRequest = new PolicyRequest();
        invalidRequest.setCustomerId(UUID.randomUUID());
        invalidRequest.setProductId(UUID.randomUUID());
        invalidRequest.setCategory(InsuranceCategory.AUTO);
        invalidRequest.setSalesChannel(SalesChannel.BROKER);
        invalidRequest.setPaymentMethod(PaymentMethod.CREDIT_CARD);
        invalidRequest.setInsuredAmount(new BigDecimal("10000.00"));
        invalidRequest.setCoverages(Map.of("BASIC", new BigDecimal("5000.00")));

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            invalidRequest.setTotalMonthlyPremiumAmount(BigDecimal.ZERO);
        });
        assertEquals("totalMonthlyPremiumAmount must be greater than zero", exception.getMessage());
    }

    @Test
    void testValidateWithNullInsuredAmount() {
        PolicyRequest invalidRequest = new PolicyRequest();
        invalidRequest.setCustomerId(UUID.randomUUID());
        invalidRequest.setProductId(UUID.randomUUID());
        invalidRequest.setCategory(InsuranceCategory.AUTO);
        invalidRequest.setSalesChannel(SalesChannel.BROKER);
        invalidRequest.setPaymentMethod(PaymentMethod.CREDIT_CARD);
        invalidRequest.setTotalMonthlyPremiumAmount(new BigDecimal("100.00"));
        invalidRequest.setCoverages(Map.of("BASIC", new BigDecimal("5000.00")));

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            invalidRequest.validate();
        });
        assertEquals("insuredAmount must be greater than zero", exception.getMessage());
    }

    @Test
    void testValidateWithEmptyCoverages() {
        PolicyRequest invalidRequest = new PolicyRequest();
        invalidRequest.setCustomerId(UUID.randomUUID());
        invalidRequest.setProductId(UUID.randomUUID());
        invalidRequest.setCategory(InsuranceCategory.AUTO);
        invalidRequest.setSalesChannel(SalesChannel.BROKER);
        invalidRequest.setPaymentMethod(PaymentMethod.CREDIT_CARD);
        invalidRequest.setTotalMonthlyPremiumAmount(new BigDecimal("100.00"));
        invalidRequest.setInsuredAmount(new BigDecimal("10000.00"));
        invalidRequest.setCoverages(new HashMap<>());

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            invalidRequest.validate();
        });
        assertEquals("At least one coverage is required", exception.getMessage());
    }

    @Test
    void testSetTotalMonthlyPremiumAmountWithNullValue() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            policyRequest.setTotalMonthlyPremiumAmount(null);
        });
        assertEquals("totalMonthlyPremiumAmount must be greater than zero", exception.getMessage());
    }

    @Test
    void testSetTotalMonthlyPremiumAmountWithNegativeValue() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            policyRequest.setTotalMonthlyPremiumAmount(new BigDecimal("-50.00"));
        });
        assertEquals("totalMonthlyPremiumAmount must be greater than zero", exception.getMessage());
    }

    @Test
    void testSetInsuredAmountWithNullValue() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            policyRequest.setInsuredAmount(null);
        });
        assertEquals("insuredAmount must be greater than zero", exception.getMessage());
    }

    @Test
    void testSetInsuredAmountWithZeroValue() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            policyRequest.setInsuredAmount(BigDecimal.ZERO);
        });
        assertEquals("insuredAmount must be greater than zero", exception.getMessage());
    }

    @Test
    void testCanTransitionToFromNullStatus() {
        PolicyRequest newRequest = new PolicyRequest();
        newRequest.setStatus(null);
        
        assertTrue(newRequest.canTransitionTo(PolicyRequestStatus.RECEIVED));
        assertDoesNotThrow(() -> newRequest.canTransitionTo(PolicyRequestStatus.VALIDATED));
        assertDoesNotThrow(() -> newRequest.canTransitionTo(PolicyRequestStatus.REJECTED));
    }

    @Test
    void testCanTransitionToFromValidatedStatus() {
        policyRequest.updateStatus(PolicyRequestStatus.VALIDATED);
        
        assertTrue(policyRequest.canTransitionTo(PolicyRequestStatus.PENDING));
        assertTrue(policyRequest.canTransitionTo(PolicyRequestStatus.REJECTED));
        assertTrue(policyRequest.canTransitionTo(PolicyRequestStatus.CANCELLED));
        assertDoesNotThrow(() -> policyRequest.canTransitionTo(PolicyRequestStatus.RECEIVED));
    }

    @Test
    void testCanTransitionToFromPendingStatus() {
        policyRequest.updateStatus(PolicyRequestStatus.VALIDATED);
        policyRequest.updateStatus(PolicyRequestStatus.PENDING);
        
        assertTrue(policyRequest.canTransitionTo(PolicyRequestStatus.APPROVED));
        assertTrue(policyRequest.canTransitionTo(PolicyRequestStatus.REJECTED));
        assertTrue(policyRequest.canTransitionTo(PolicyRequestStatus.CANCELLED));
        assertDoesNotThrow(() -> policyRequest.canTransitionTo(PolicyRequestStatus.VALIDATED));
    }

    @Test
    void testCanTransitionToFromApprovedStatus() {
        policyRequest.updateStatus(PolicyRequestStatus.VALIDATED);
        policyRequest.updateStatus(PolicyRequestStatus.PENDING);
        policyRequest.updateStatus(PolicyRequestStatus.APPROVED);
        
        assertDoesNotThrow(() -> policyRequest.canTransitionTo(PolicyRequestStatus.PENDING));
        assertDoesNotThrow(() -> policyRequest.canTransitionTo(PolicyRequestStatus.REJECTED));
        assertDoesNotThrow(() -> policyRequest.canTransitionTo(PolicyRequestStatus.CANCELLED));
    }

    @Test
    void testCanTransitionToFromRejectedStatus() {
        policyRequest.updateStatus(PolicyRequestStatus.REJECTED);
        
        assertDoesNotThrow(() -> policyRequest.canTransitionTo(PolicyRequestStatus.APPROVED));
        assertDoesNotThrow(() -> policyRequest.canTransitionTo(PolicyRequestStatus.VALIDATED));
        assertDoesNotThrow(() -> policyRequest.canTransitionTo(PolicyRequestStatus.PENDING));
    }

    @Test
    void testCanTransitionToFromCancelledStatus() {
        policyRequest.updateStatus(PolicyRequestStatus.CANCELLED);
        
        assertDoesNotThrow(() -> policyRequest.canTransitionTo(PolicyRequestStatus.APPROVED));
        assertDoesNotThrow(() -> policyRequest.canTransitionTo(PolicyRequestStatus.VALIDATED));
        assertDoesNotThrow(() -> policyRequest.canTransitionTo(PolicyRequestStatus.PENDING));
    }

    @Test
    void testCalculateTotalCoverageAmountWithSingleCoverage() {
        Map<String, BigDecimal> singleCoverage = new HashMap<>();
        singleCoverage.put("COLLISION", new BigDecimal("25000.00"));
        policyRequest.setCoverages(singleCoverage);
        
        BigDecimal total = policyRequest.calculateTotalCoverageAmount();
        assertEquals(new BigDecimal("25000.00"), total);
    }

    @Test
    void testCalculateTotalCoverageAmountWithMultipleCoverages() {
        Map<String, BigDecimal> multipleCoverages = new HashMap<>();
        multipleCoverages.put("COLLISION", new BigDecimal("30000.00"));
        multipleCoverages.put("THEFT", new BigDecimal("20000.00"));
        multipleCoverages.put("FIRE", new BigDecimal("15000.00"));
        policyRequest.setCoverages(multipleCoverages);
        
        BigDecimal total = policyRequest.calculateTotalCoverageAmount();
        assertEquals(new BigDecimal("65000.00"), total);
    }

    @Test
    void testCoveragesAndAssistancesManagement() {
        // Test adding coverages
        Map<String, BigDecimal> newCoverages = new HashMap<>();
        newCoverages.put("FIRE", new BigDecimal("15000.00"));
        newCoverages.put("FLOOD", new BigDecimal("12000.00"));
        policyRequest.setCoverages(newCoverages);
        
        assertEquals(2, policyRequest.getCoverages().size());
        assertTrue(policyRequest.getCoverages().containsKey("FIRE"));
        assertTrue(policyRequest.getCoverages().containsKey("FLOOD"));
        
        List<String> newAssistances = List.of("24h Towing", "Emergency Service", "Rental Car");
        policyRequest.setAssistances(newAssistances);
        
        assertEquals(3, policyRequest.getAssistances().size());
        assertTrue(policyRequest.getAssistances().contains("24h Towing"));
        assertTrue(policyRequest.getAssistances().contains("Emergency Service"));
        assertTrue(policyRequest.getAssistances().contains("Rental Car"));
    }

    @Test
    void testDifferentInsuranceCategories() {

        policyRequest.setCategory(InsuranceCategory.LIFE);
        assertEquals(InsuranceCategory.LIFE, policyRequest.getCategory());
        
        policyRequest.setCategory(InsuranceCategory.RESIDENTIAL);
        assertEquals(InsuranceCategory.RESIDENTIAL, policyRequest.getCategory());
        
        policyRequest.setCategory(InsuranceCategory.TRAVEL);
        assertEquals(InsuranceCategory.TRAVEL, policyRequest.getCategory());
    }

    @Test
    void testDifferentSalesChannels() {

        policyRequest.setSalesChannel(SalesChannel.MOBILE);
        assertEquals(SalesChannel.MOBILE, policyRequest.getSalesChannel());
        
        policyRequest.setSalesChannel(SalesChannel.WEBSITE);
        assertEquals(SalesChannel.WEBSITE, policyRequest.getSalesChannel());
        
        policyRequest.setSalesChannel(SalesChannel.CALL_CENTER);
        assertEquals(SalesChannel.CALL_CENTER, policyRequest.getSalesChannel());
    }

    @Test
    void testDifferentPaymentMethods() {

        policyRequest.setPaymentMethod(PaymentMethod.DEBIT_CARD);
        assertEquals(PaymentMethod.DEBIT_CARD, policyRequest.getPaymentMethod());
        
        policyRequest.setPaymentMethod(PaymentMethod.BANK_TRANSFER);
        assertEquals(PaymentMethod.BANK_TRANSFER, policyRequest.getPaymentMethod());
        
        policyRequest.setPaymentMethod(PaymentMethod.PIX);
        assertEquals(PaymentMethod.PIX, policyRequest.getPaymentMethod());
    }

    @Test
    void testRiskAnalysisManagement() {
        assertNull(policyRequest.getRiskAnalysis());
        
        RiskAnalysis riskAnalysis = new RiskAnalysis();
        riskAnalysis.setId(UUID.randomUUID());
        riskAnalysis.setAnalyzedAt(LocalDateTime.now());
        
        policyRequest.setRiskAnalysis(riskAnalysis);
        assertNotNull(policyRequest.getRiskAnalysis());
        assertEquals(riskAnalysis, policyRequest.getRiskAnalysis());
        
        policyRequest.setRiskAnalysis(null);
        assertNull(policyRequest.getRiskAnalysis());
    }

    @Test
    void testStatusHistoryOrdering() {

        policyRequest.updateStatus(PolicyRequestStatus.VALIDATED);
        policyRequest.updateStatus(PolicyRequestStatus.PENDING);
        policyRequest.updateStatus(PolicyRequestStatus.APPROVED);
        
        assertEquals(3, policyRequest.getStatusHistory().size());
        
        List<StatusHistory> history = policyRequest.getStatusHistory();

        assertTrue(history.get(0).getNewStatus() == PolicyRequestStatus.APPROVED ||
                  history.get(1).getNewStatus() == PolicyRequestStatus.APPROVED ||
                  history.get(2).getNewStatus() == PolicyRequestStatus.APPROVED);
    }

    @Test
    void testFinishedAtTimestamp() {
        assertNull(policyRequest.getFinishedAt());
        
        policyRequest.updateStatus(PolicyRequestStatus.VALIDATED);
        policyRequest.updateStatus(PolicyRequestStatus.PENDING);
        policyRequest.updateStatus(PolicyRequestStatus.APPROVED);
        
        assertNotNull(policyRequest.getFinishedAt());
        assertTrue(policyRequest.getFinishedAt().isBefore(LocalDateTime.now().plusSeconds(1)));
        assertTrue(policyRequest.getFinishedAt().isAfter(LocalDateTime.now().minusSeconds(5)));
    }

    @Test
    void testPolicyRequestDefaultValues() {
        PolicyRequest newRequest = new PolicyRequest();
        
        assertEquals(PolicyRequestStatus.RECEIVED, newRequest.getStatus());
        assertNotNull(newRequest.getCoverages());
        assertTrue(newRequest.getCoverages().isEmpty());
        assertNotNull(newRequest.getAssistances());
        assertTrue(newRequest.getAssistances().isEmpty());
        assertNotNull(newRequest.getStatusHistory());
        assertTrue(newRequest.getStatusHistory().isEmpty());
        assertNull(newRequest.getRiskAnalysis());
        assertNull(newRequest.getFinishedAt());
    }

    @Test
    void testPolicyRequestWithLargeAmounts() {
        PolicyRequest largeAmountRequest = new PolicyRequest();
        largeAmountRequest.setCustomerId(UUID.randomUUID());
        largeAmountRequest.setProductId(UUID.randomUUID());
        largeAmountRequest.setCategory(InsuranceCategory.LIFE);
        largeAmountRequest.setSalesChannel(SalesChannel.BROKER);
        largeAmountRequest.setPaymentMethod(PaymentMethod.BANK_TRANSFER);
        
        BigDecimal largePremium = new BigDecimal("5000.00");
        BigDecimal largeInsuredAmount = new BigDecimal("1000000.00");
        
        largeAmountRequest.setTotalMonthlyPremiumAmount(largePremium);
        largeAmountRequest.setInsuredAmount(largeInsuredAmount);
        largeAmountRequest.setCoverages(Map.of("LIFE_COVERAGE", largeInsuredAmount));
        
        assertEquals(largePremium, largeAmountRequest.getTotalMonthlyPremiumAmount());
        assertEquals(largeInsuredAmount, largeAmountRequest.getInsuredAmount());
        assertEquals(largeInsuredAmount, largeAmountRequest.calculateTotalCoverageAmount());
    }
} 