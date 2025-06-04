package com.insurance.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.insurance.domain.enums.InsuranceCategory;
import com.insurance.domain.enums.PaymentMethod;
import com.insurance.domain.enums.PolicyRequestStatus;
import com.insurance.domain.enums.SalesChannel;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PolicyRequestDTOTest {

    private Validator validator;
    private PolicyRequestDTO policyRequestDTO;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
        
        policyRequestDTO = new PolicyRequestDTO();
        policyRequestDTO.setId(UUID.randomUUID());
        policyRequestDTO.setCustomerId(UUID.randomUUID());
        policyRequestDTO.setProductId(UUID.randomUUID());
        policyRequestDTO.setCategory(InsuranceCategory.AUTO);
        policyRequestDTO.setSalesChannel(SalesChannel.MOBILE);
        policyRequestDTO.setPaymentMethod(PaymentMethod.CREDIT_CARD);
        policyRequestDTO.setStatus(PolicyRequestStatus.RECEIVED);
        policyRequestDTO.setTotalMonthlyPremiumAmount(BigDecimal.valueOf(150.00));
        policyRequestDTO.setInsuredAmount(BigDecimal.valueOf(50000.00));
        
        Map<String, BigDecimal> coverages = new HashMap<>();
        coverages.put("RCF", BigDecimal.valueOf(100000.00));
        policyRequestDTO.setCoverages(coverages);
        
        List<String> assistances = new ArrayList<>();
        assistances.add("24h Assistance");
        policyRequestDTO.setAssistances(assistances);
        
        List<StatusHistoryDTO> history = new ArrayList<>();
        StatusHistoryDTO historyItem = new StatusHistoryDTO();
        historyItem.setStatus(PolicyRequestStatus.RECEIVED);
        historyItem.setTimestamp(LocalDateTime.now());
        history.add(historyItem);
        policyRequestDTO.setHistory(history);
    }

    @Test
    void testGettersAndSetters() {
        UUID id = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        LocalDateTime createdAt = LocalDateTime.now();
        LocalDateTime finishedAt = LocalDateTime.now().plusDays(1);
        BigDecimal premium = BigDecimal.valueOf(200.00);
        BigDecimal insuredAmount = BigDecimal.valueOf(75000.00);

        policyRequestDTO.setId(id);
        policyRequestDTO.setCustomerId(customerId);
        policyRequestDTO.setProductId(productId);
        policyRequestDTO.setCategory(InsuranceCategory.LIFE);
        policyRequestDTO.setSalesChannel(SalesChannel.MOBILE);
        policyRequestDTO.setPaymentMethod(PaymentMethod.BANK_TRANSFER);
        policyRequestDTO.setStatus(PolicyRequestStatus.VALIDATED);
        policyRequestDTO.setCreatedAt(createdAt);
        policyRequestDTO.setFinishedAt(finishedAt);
        policyRequestDTO.setTotalMonthlyPremiumAmount(premium);
        policyRequestDTO.setInsuredAmount(insuredAmount);

        assertEquals(id, policyRequestDTO.getId());
        assertEquals(customerId, policyRequestDTO.getCustomerId());
        assertEquals(productId, policyRequestDTO.getProductId());
        assertEquals(InsuranceCategory.LIFE, policyRequestDTO.getCategory());
        assertEquals(SalesChannel.MOBILE, policyRequestDTO.getSalesChannel());
        assertEquals(PaymentMethod.BANK_TRANSFER, policyRequestDTO.getPaymentMethod());
        assertEquals(PolicyRequestStatus.VALIDATED, policyRequestDTO.getStatus());
        assertEquals(createdAt, policyRequestDTO.getCreatedAt());
        assertEquals(finishedAt, policyRequestDTO.getFinishedAt());
        assertEquals(premium, policyRequestDTO.getTotalMonthlyPremiumAmount());
        assertEquals(insuredAmount, policyRequestDTO.getInsuredAmount());
    }

    @Test
    void testValidDTO() {
        Set<ConstraintViolation<PolicyRequestDTO>> violations = validator.validate(policyRequestDTO);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testNullCustomerId() {
        policyRequestDTO.setCustomerId(null);
        
        Set<ConstraintViolation<PolicyRequestDTO>> violations = validator.validate(policyRequestDTO);
        
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
            .anyMatch(v -> v.getMessage().equals("Customer ID is required")));
    }

    @Test
    void testNullProductId() {
        policyRequestDTO.setProductId(null);
        
        Set<ConstraintViolation<PolicyRequestDTO>> violations = validator.validate(policyRequestDTO);
        
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
            .anyMatch(v -> v.getMessage().equals("Product ID is required")));
    }

    @Test
    void testNullCategory() {
        policyRequestDTO.setCategory(null);
        
        Set<ConstraintViolation<PolicyRequestDTO>> violations = validator.validate(policyRequestDTO);
        
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
            .anyMatch(v -> v.getMessage().equals("Category is required")));
    }

    @Test
    void testNullSalesChannel() {
        policyRequestDTO.setSalesChannel(null);
        
        Set<ConstraintViolation<PolicyRequestDTO>> violations = validator.validate(policyRequestDTO);
        
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
            .anyMatch(v -> v.getMessage().equals("Sales channel is required")));
    }

    @Test
    void testNullPaymentMethod() {
        policyRequestDTO.setPaymentMethod(null);
        
        Set<ConstraintViolation<PolicyRequestDTO>> violations = validator.validate(policyRequestDTO);
        
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
            .anyMatch(v -> v.getMessage().equals("Payment method is required")));
    }

    @Test
    void testNullTotalMonthlyPremiumAmount() {
        policyRequestDTO.setTotalMonthlyPremiumAmount(null);
        
        Set<ConstraintViolation<PolicyRequestDTO>> violations = validator.validate(policyRequestDTO);
        
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
            .anyMatch(v -> v.getMessage().equals("Total monthly premium amount is required")));
    }

    @Test
    void testNegativeTotalMonthlyPremiumAmount() {
        policyRequestDTO.setTotalMonthlyPremiumAmount(BigDecimal.valueOf(-100.00));
        
        Set<ConstraintViolation<PolicyRequestDTO>> violations = validator.validate(policyRequestDTO);
        
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
            .anyMatch(v -> v.getMessage().equals("Total monthly premium amount must be positive")));
    }

    @Test
    void testNullInsuredAmount() {
        policyRequestDTO.setInsuredAmount(null);
        
        Set<ConstraintViolation<PolicyRequestDTO>> violations = validator.validate(policyRequestDTO);
        
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
            .anyMatch(v -> v.getMessage().equals("Insured amount is required")));
    }

    @Test
    void testNegativeInsuredAmount() {
        policyRequestDTO.setInsuredAmount(BigDecimal.valueOf(-50000.00));
        
        Set<ConstraintViolation<PolicyRequestDTO>> violations = validator.validate(policyRequestDTO);
        
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
            .anyMatch(v -> v.getMessage().equals("Insured amount must be positive")));
    }

    @Test
    void testNullCoverages() {
        policyRequestDTO.setCoverages(null);
        
        Set<ConstraintViolation<PolicyRequestDTO>> violations = validator.validate(policyRequestDTO);
        
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
            .anyMatch(v -> v.getMessage().equals("Coverages are required")));
    }

    @Test
    void testCoveragesAndAssistances() {
        Map<String, BigDecimal> coverages = new HashMap<>();
        coverages.put("RCF", BigDecimal.valueOf(100000.00));
        coverages.put("APP", BigDecimal.valueOf(50000.00));
        
        List<String> assistances = new ArrayList<>();
        assistances.add("24h Assistance");
        assistances.add("Glass Coverage");
        
        policyRequestDTO.setCoverages(coverages);
        policyRequestDTO.setAssistances(assistances);
        
        assertEquals(2, policyRequestDTO.getCoverages().size());
        assertEquals(2, policyRequestDTO.getAssistances().size());
        assertTrue(policyRequestDTO.getCoverages().containsKey("RCF"));
        assertTrue(policyRequestDTO.getAssistances().contains("24h Assistance"));
    }

    @Test
    void testEqualsAndHashCode() {
        PolicyRequestDTO dto1 = new PolicyRequestDTO();
        PolicyRequestDTO dto2 = new PolicyRequestDTO();
        
        UUID id = UUID.randomUUID();
        dto1.setId(id);
        dto2.setId(id);
        dto1.setCustomerId(UUID.randomUUID());
        dto2.setCustomerId(dto1.getCustomerId());
        
        assertEquals(dto1, dto2);
        assertEquals(dto1.hashCode(), dto2.hashCode());
        
        dto2.setId(UUID.randomUUID());
        assertNotEquals(dto1, dto2);
    }

    @Test
    void testToString() {
        String toString = policyRequestDTO.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("PolicyRequestDTO"));
    }

    @Test
    void testDefaultCollections() {
        PolicyRequestDTO dto = new PolicyRequestDTO();
        assertNotNull(dto.getCoverages());
        assertNotNull(dto.getAssistances());
        assertTrue(dto.getCoverages().isEmpty());
        assertTrue(dto.getAssistances().isEmpty());
    }
} 