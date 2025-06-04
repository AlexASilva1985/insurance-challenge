package com.insurance.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.insurance.domain.PolicyRequest;
import com.insurance.domain.StatusHistory;
import com.insurance.domain.enums.InsuranceCategory;
import com.insurance.domain.enums.PaymentMethod;
import com.insurance.domain.enums.PolicyRequestStatus;
import com.insurance.domain.enums.SalesChannel;
import com.insurance.dto.PolicyRequestDTO;
import com.insurance.dto.StatusHistoryDTO;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PolicyRequestMapperTest {

    private PolicyRequestMapper mapper;
    private UUID customerId;
    private UUID productId;
    private Map<String, BigDecimal> coverages;
    private List<String> assistances;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        mapper = new PolicyRequestMapper();
        customerId = UUID.randomUUID();
        productId = UUID.randomUUID();
        now = LocalDateTime.now();

        coverages = new HashMap<>();
        coverages.put("Collision", BigDecimal.valueOf(30000));
        coverages.put("Theft", BigDecimal.valueOf(20000));

        assistances = Arrays.asList("Roadside Assistance", "Glass Protection");
    }

    @Test
    void testToEntityWithNullDTO() {
        assertNull(mapper.toEntity(null));
    }

    @Test
    void testToDTOWithNullEntity() {
        assertNull(mapper.toDTO(null));
    }

    @Test
    void testToEntity() {
        PolicyRequestDTO dto = createSampleDTO();
        PolicyRequest entity = mapper.toEntity(dto);

        assertNotNull(entity);
        assertEquals(customerId, entity.getCustomerId());
        assertEquals(productId, entity.getProductId());
        assertEquals(InsuranceCategory.AUTO, entity.getCategory());
        assertEquals(SalesChannel.MOBILE, entity.getSalesChannel());
        assertEquals(PaymentMethod.CREDIT_CARD, entity.getPaymentMethod());
        assertEquals(BigDecimal.valueOf(150.00), entity.getTotalMonthlyPremiumAmount());
        assertEquals(BigDecimal.valueOf(50000.00), entity.getInsuredAmount());
        assertEquals(coverages, entity.getCoverages());
        assertEquals(assistances, entity.getAssistances());
    }

    @Test
    void testToDTO() {
        PolicyRequest entity = createSampleEntity();
        PolicyRequestDTO dto = mapper.toDTO(entity);

        assertNotNull(dto);
        assertEquals(entity.getId(), dto.getId());
        assertEquals(customerId, dto.getCustomerId());
        assertEquals(productId, dto.getProductId());
        assertEquals(InsuranceCategory.AUTO, dto.getCategory());
        assertEquals(SalesChannel.MOBILE, dto.getSalesChannel());
        assertEquals(PaymentMethod.CREDIT_CARD, dto.getPaymentMethod());
        assertEquals(PolicyRequestStatus.RECEIVED, dto.getStatus());
        assertEquals(now, dto.getCreatedAt());
        assertEquals(now.plusDays(1), dto.getFinishedAt());
        assertEquals(BigDecimal.valueOf(150.00), dto.getTotalMonthlyPremiumAmount());
        assertEquals(BigDecimal.valueOf(50000.00), dto.getInsuredAmount());
        assertEquals(coverages, dto.getCoverages());
        assertEquals(assistances, dto.getAssistances());
    }

    @Test
    void testToDTOWithStatusHistory() {
        PolicyRequest entity = createSampleEntity();
        List<StatusHistory> history = new ArrayList<>();
        
        StatusHistory statusHistory = new StatusHistory();
        statusHistory.setNewStatus(PolicyRequestStatus.VALIDATED);
        statusHistory.setChangedAt(now);
        history.add(statusHistory);
        
        entity.setStatusHistory(history);

        PolicyRequestDTO dto = mapper.toDTO(entity);

        assertNotNull(dto.getHistory());
        assertEquals(1, dto.getHistory().size());
        
        StatusHistoryDTO historyDTO = dto.getHistory().get(0);
        assertEquals(PolicyRequestStatus.VALIDATED, historyDTO.getStatus());
        assertEquals(now, historyDTO.getTimestamp());
    }

    @Test
    void testToDTOWithNullStatusHistory() {
        PolicyRequest entity = createSampleEntity();
        entity.setStatusHistory(null);

        PolicyRequestDTO dto = mapper.toDTO(entity);

        assertNotNull(dto);
        assertNull(dto.getHistory());
    }

    private PolicyRequestDTO createSampleDTO() {
        PolicyRequestDTO dto = new PolicyRequestDTO();
        dto.setCustomerId(customerId);
        dto.setProductId(productId);
        dto.setCategory(InsuranceCategory.AUTO);
        dto.setSalesChannel(SalesChannel.MOBILE);
        dto.setPaymentMethod(PaymentMethod.CREDIT_CARD);
        dto.setTotalMonthlyPremiumAmount(BigDecimal.valueOf(150.00));
        dto.setInsuredAmount(BigDecimal.valueOf(50000.00));
        dto.setCoverages(new HashMap<>(coverages));
        dto.setAssistances(new ArrayList<>(assistances));
        return dto;
    }

    private PolicyRequest createSampleEntity() {
        PolicyRequest entity = new PolicyRequest();
        entity.setId(UUID.randomUUID());
        entity.setCustomerId(customerId);
        entity.setProductId(productId);
        entity.setCategory(InsuranceCategory.AUTO);
        entity.setSalesChannel(SalesChannel.MOBILE);
        entity.setPaymentMethod(PaymentMethod.CREDIT_CARD);
        entity.setStatus(PolicyRequestStatus.RECEIVED);
        entity.setCreatedAt(now);
        entity.setFinishedAt(now.plusDays(1));
        entity.setTotalMonthlyPremiumAmount(BigDecimal.valueOf(150.00));
        entity.setInsuredAmount(BigDecimal.valueOf(50000.00));
        entity.setCoverages(new HashMap<>(coverages));
        entity.setAssistances(new ArrayList<>(assistances));
        return entity;
    }
} 