package com.insurance.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.insurance.domain.PolicyRequest;
import com.insurance.domain.enums.InsuranceCategory;
import com.insurance.domain.enums.PaymentMethod;
import com.insurance.domain.enums.PolicyRequestStatus;
import com.insurance.domain.enums.SalesChannel;
import com.insurance.dto.PolicyRequestDTO;
import com.insurance.mapper.PolicyRequestMapper;
import com.insurance.service.PolicyRequestService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class PolicyRequestControllerTest {

    private MockMvc mockMvc;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private PolicyRequestService service;

    @Mock
    private PolicyRequestMapper mapper;

    @InjectMocks
    private PolicyRequestController controller;

    private PolicyRequestDTO requestDTO;
    private PolicyRequest policyRequest;
    private UUID policyId;
    private UUID customerId;

    @BeforeEach
    void setUp() throws Exception {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .build();

        policyId = UUID.randomUUID();
        customerId = UUID.randomUUID();

        requestDTO = new PolicyRequestDTO();
        requestDTO.setCustomerId(customerId);
        requestDTO.setProductId(UUID.randomUUID());
        requestDTO.setCategory(InsuranceCategory.AUTO);
        requestDTO.setSalesChannel(SalesChannel.MOBILE);
        requestDTO.setPaymentMethod(PaymentMethod.CREDIT_CARD);
        requestDTO.setTotalMonthlyPremiumAmount(new BigDecimal("150.00"));
        requestDTO.setInsuredAmount(new BigDecimal("50000.00"));
        requestDTO.setCoverages(new HashMap<>() {{
            put("Collision", new BigDecimal("30000.00"));
            put("Theft", new BigDecimal("20000.00"));
        }});
        requestDTO.setAssistances(Arrays.asList("Roadside Assistance", "Glass Protection"));

        policyRequest = new PolicyRequest();
        policyRequest.setId(policyId);
        policyRequest.setCustomerId(customerId);
        policyRequest.setStatus(PolicyRequestStatus.RECEIVED);
        policyRequest.setCategory(InsuranceCategory.AUTO);
    }

    @Test
    void testCreatePolicyRequestCreatedStatus() throws Exception {
        when(mapper.toEntity(any(PolicyRequestDTO.class))).thenReturn(policyRequest);
        when(service.createPolicyRequest(any(PolicyRequest.class))).thenReturn(policyRequest);
        when(mapper.toDTO(any(PolicyRequest.class))).thenReturn(requestDTO);

        mockMvc.perform(post("/api/v1/policy-requests")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.customerId").value(customerId.toString()))
                .andExpect(jsonPath("$.category").value(InsuranceCategory.AUTO.name()));

        verify(service).createPolicyRequest(any(PolicyRequest.class));
    }

    @Test
    void testGetPolicyRequest() throws Exception {
        when(service.findById(policyId)).thenReturn(policyRequest);
        when(mapper.toDTO(any(PolicyRequest.class))).thenReturn(requestDTO);

        mockMvc.perform(get("/api/v1/policy-requests/{id}", policyId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerId").value(customerId.toString()));

        verify(service).findById(policyId);
    }

    @Test
    void testGetPolicyRequestsByCustomerList() throws Exception {
        List<PolicyRequest> policyRequests = Arrays.asList(policyRequest);
        when(service.findByCustomerId(customerId)).thenReturn(policyRequests);
        when(mapper.toDTO(any(PolicyRequest.class))).thenReturn(requestDTO);

        mockMvc.perform(get("/api/v1/policy-requests/customer/{customerId}", customerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].customerId").value(customerId.toString()));

        verify(service).findByCustomerId(customerId);
    }

    @Test
    void testValidateReturnOk() throws Exception {
        doNothing().when(service).validatePolicyRequest(policyId);

        mockMvc.perform(post("/api/v1/policy-requests/{id}/validate", policyId))
                .andExpect(status().isOk());

        verify(service).validatePolicyRequest(policyId);
    }

    @Test
    void testProcessFraudAnalysisReturnOk() throws Exception {
        doNothing().when(service).processFraudAnalysis(policyId);

        mockMvc.perform(post("/api/v1/policy-requests/{id}/fraud-analysis", policyId))
                .andExpect(status().isOk());

        verify(service).processFraudAnalysis(policyId);
    }

    @Test
    void testProcessPaymentReturnOk() throws Exception {
        doNothing().when(service).processPayment(policyId);

        mockMvc.perform(post("/api/v1/policy-requests/{id}/payment", policyId))
                .andExpect(status().isOk());

        verify(service).processPayment(policyId);
    }

    @Test
    void testProcessSubscriptionReturnOk() throws Exception {
        doNothing().when(service).processSubscription(policyId);

        mockMvc.perform(post("/api/v1/policy-requests/{id}/subscription", policyId))
                .andExpect(status().isOk());

        verify(service).processSubscription(policyId);
    }

    @Test
    void testCancelPolicyRequestReturnOk() throws Exception {
        doNothing().when(service).cancelPolicyRequest(policyId);

        mockMvc.perform(post("/api/v1/policy-requests/{id}/cancel", policyId))
                .andExpect(status().isOk());

        verify(service).cancelPolicyRequest(policyId);
    }

    @Test
    void testCreatePolicyRequestReturnBadRequest() throws Exception {
        requestDTO.setCustomerId(null); // Tornando o DTO inválido

        mockMvc.perform(post("/api/v1/policy-requests")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isBadRequest());

        verify(service, never()).createPolicyRequest(any(PolicyRequest.class));
    }

    @Test
    void testGetPolicyRequestReturnNotFound() throws Exception {
        when(service.findById(any(UUID.class))).thenThrow(new EntityNotFoundException("Policy request not found"));

        mockMvc.perform(get("/api/v1/policy-requests/{id}", UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }
}