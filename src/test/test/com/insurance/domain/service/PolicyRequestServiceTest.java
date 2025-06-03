package com.insurance.domain.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.insurance.domain.PolicyRequest;
import com.insurance.domain.RiskAnalysis;
import com.insurance.domain.enums.CustomerRiskType;
import com.insurance.domain.enums.InsuranceCategory;
import com.insurance.domain.enums.PolicyRequestStatus;
import com.insurance.repository.PolicyRequestRepository;
import com.insurance.service.FraudAnalysisService;
import com.insurance.service.PaymentService;
import com.insurance.service.SubscriptionService;
import com.insurance.service.impl.PolicyRequestServiceImpl;
import com.insurance.infrastructure.messaging.service.EventPublisher;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PolicyRequestServiceTest {

    @Mock
    private PolicyRequestRepository repository;

    @Mock
    private FraudAnalysisService fraudAnalysisService;

    @Mock
    private PaymentService paymentService;

    @Mock
    private SubscriptionService subscriptionService;

    @Mock
    private EventPublisher eventPublisher;

    @InjectMocks
    private PolicyRequestServiceImpl service;

    private PolicyRequest policyRequest;
    private UUID policyId;

    @BeforeEach
    void setUp() {
        policyId = UUID.randomUUID();
        policyRequest = new PolicyRequest();
        policyRequest.setId(policyId);
        policyRequest.setCustomerId(UUID.randomUUID());
        policyRequest.setProductId(UUID.randomUUID());
        policyRequest.setCategory(InsuranceCategory.AUTO);
        policyRequest.setInsuredAmount(new BigDecimal("300000.00"));
        policyRequest.setStatus(PolicyRequestStatus.RECEIVED);
    }

    @Test
    void shouldCreatePolicyRequest() {
        when(repository.save(any(PolicyRequest.class))).thenReturn(policyRequest);

        PolicyRequest created = service.createPolicyRequest(policyRequest);

        assertNotNull(created);
        assertEquals(PolicyRequestStatus.RECEIVED, created.getStatus());
        verify(repository).save(policyRequest);
        verify(eventPublisher).publish(any(), any(), any());
    }

    @Test
    void shouldProcessFraudAnalysisAndValidateSuccessfully() {
        RiskAnalysis riskAnalysis = new RiskAnalysis();
        riskAnalysis.setClassification(CustomerRiskType.REGULAR);

        when(repository.findById(policyId)).thenReturn(Optional.of(policyRequest));
        when(fraudAnalysisService.analyzeFraud(any())).thenReturn(riskAnalysis);
        when(repository.save(any(PolicyRequest.class))).thenReturn(policyRequest);

        service.processFraudAnalysis(policyId);

        verify(fraudAnalysisService).analyzeFraud(any());
        verify(repository, times(2)).save(any(PolicyRequest.class));
        verify(eventPublisher, atLeastOnce()).publish(any(), any(), any());
    }

    @Test
    void shouldRejectPolicyRequestWhenAmountExceedsLimit() {
        RiskAnalysis riskAnalysis = new RiskAnalysis();
        riskAnalysis.setClassification(CustomerRiskType.REGULAR);
        policyRequest.setRiskAnalysis(riskAnalysis);
        policyRequest.setInsuredAmount(new BigDecimal("1000000.00")); // Excede o limite

        when(repository.findById(policyId)).thenReturn(Optional.of(policyRequest));
        when(repository.save(any(PolicyRequest.class))).thenReturn(policyRequest);

        service.validatePolicyRequest(policyId);

        assertEquals(PolicyRequestStatus.REJECTED, policyRequest.getStatus());
        verify(repository).save(policyRequest);
        verify(eventPublisher).publish(any(), any(), any());
    }

    @Test
    void shouldProcessPaymentSuccessfully() {
        policyRequest.setStatus(PolicyRequestStatus.VALIDATED); // Ajustando o estado inicial
        when(repository.findById(policyId)).thenReturn(Optional.of(policyRequest));
        when(repository.save(any(PolicyRequest.class))).thenReturn(policyRequest);
        doNothing().when(paymentService).processPayment(any());

        service.processPayment(policyId);

        assertEquals(PolicyRequestStatus.PENDING, policyRequest.getStatus());
        verify(paymentService).processPayment(policyRequest);
        verify(repository).save(policyRequest);
        verify(eventPublisher).publish(any(), any(), any());
    }

    @Test
    void shouldApproveSubscription() {
        policyRequest.setStatus(PolicyRequestStatus.PENDING); // Ajustando o estado inicial
        when(repository.findById(policyId)).thenReturn(Optional.of(policyRequest));
        when(repository.save(any(PolicyRequest.class))).thenReturn(policyRequest);
        doNothing().when(subscriptionService).processSubscription(any());

        service.processSubscription(policyId);

        assertEquals(PolicyRequestStatus.APPROVED, policyRequest.getStatus());
        verify(subscriptionService).processSubscription(policyRequest);
        verify(repository).save(policyRequest);
        verify(eventPublisher).publish(any(), any(), any());
    }
} 