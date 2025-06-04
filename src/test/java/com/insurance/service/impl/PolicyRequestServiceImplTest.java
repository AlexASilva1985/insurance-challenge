package com.insurance.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.insurance.domain.PolicyRequest;
import com.insurance.domain.RiskAnalysis;
import com.insurance.domain.enums.CustomerRiskType;
import com.insurance.domain.enums.InsuranceCategory;
import com.insurance.domain.enums.PaymentMethod;
import com.insurance.domain.enums.PolicyRequestStatus;
import com.insurance.domain.enums.SalesChannel;
import com.insurance.event.PolicyRequestCreatedEvent;
import com.insurance.event.PolicyRequestEvent;
import com.insurance.infrastructure.messaging.config.RabbitMQConfig;
import com.insurance.infrastructure.messaging.service.EventPublisher;
import com.insurance.repository.PolicyRequestRepository;
import com.insurance.service.FraudAnalysisService;
import com.insurance.service.PaymentService;
import com.insurance.service.SubscriptionService;
import jakarta.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PolicyRequestServiceImplTest {

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

    @Spy
    @InjectMocks
    private PolicyRequestServiceImpl policyRequestService;

    @Captor
    private ArgumentCaptor<PolicyRequestEvent> eventCaptor;

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
        policyRequest.setProductId(UUID.randomUUID());
        policyRequest.setCategory(InsuranceCategory.AUTO);
        policyRequest.setSalesChannel(SalesChannel.MOBILE);
        policyRequest.setPaymentMethod(PaymentMethod.CREDIT_CARD);
        policyRequest.setStatus(PolicyRequestStatus.RECEIVED);
        policyRequest.setCreatedAt(now);
        policyRequest.setUpdatedAt(now);
        policyRequest.setCreatedBy("system");
        policyRequest.setUpdatedBy("system");
        policyRequest.setTotalMonthlyPremiumAmount(BigDecimal.valueOf(150.00));
        policyRequest.setInsuredAmount(BigDecimal.valueOf(50000.00));
    }

    @Test
    void testCreatePolicyRequest() {
        when(repository.save(any(PolicyRequest.class))).thenReturn(policyRequest);

        PolicyRequest result = policyRequestService.createPolicyRequest(policyRequest);

        assertNotNull(result);
        assertEquals(PolicyRequestStatus.RECEIVED, result.getStatus());
        verify(repository).save(policyRequest);
        verify(eventPublisher).publish(
            eq(RabbitMQConfig.POLICY_EVENTS_EXCHANGE),
            eq(RabbitMQConfig.POLICY_CREATED_KEY),
            any(PolicyRequestCreatedEvent.class)
        );
    }

    @Test
    void testFindById() {
        when(repository.findById(requestId)).thenReturn(Optional.of(policyRequest));

        PolicyRequest result = policyRequestService.findById(requestId);

        assertNotNull(result);
        assertEquals(requestId, result.getId());
    }

    @Test
    void testFindByIdNotFound() {
        when(repository.findById(requestId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> 
            policyRequestService.findById(requestId)
        );
    }

    @Test
    void testFindByCustomerId() {
        List<PolicyRequest> requests = Arrays.asList(policyRequest);
        when(repository.findByCustomerId(customerId)).thenReturn(requests);

        List<PolicyRequest> result = policyRequestService.findByCustomerId(customerId);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(customerId, result.get(0).getCustomerId());
    }

    @Test
    void testUpdateStatusValidTransition() {
        policyRequest.setStatus(PolicyRequestStatus.RECEIVED);
        when(repository.findById(requestId)).thenReturn(Optional.of(policyRequest));
        when(repository.save(any(PolicyRequest.class))).thenReturn(policyRequest);

        PolicyRequest result = policyRequestService.updateStatus(requestId, PolicyRequestStatus.VALIDATED);

        assertEquals(PolicyRequestStatus.VALIDATED, result.getStatus());
        verify(eventPublisher).publish(
            eq(RabbitMQConfig.POLICY_EVENTS_EXCHANGE),
            eq(RabbitMQConfig.POLICY_VALIDATED_KEY),
            eventCaptor.capture()
        );
    }

    @Test
    void testUpdateStatusInvalidTransition() {
        policyRequest.setStatus(PolicyRequestStatus.APPROVED);
        when(repository.findById(requestId)).thenReturn(Optional.of(policyRequest));

        assertThrows(IllegalStateException.class, () ->
            policyRequestService.updateStatus(requestId, PolicyRequestStatus.VALIDATED)
        );
    }

    @Test
    void testProcessFraudAnalysisSuccess() {
        RiskAnalysis riskAnalysis = new RiskAnalysis();
        riskAnalysis.setClassification(CustomerRiskType.REGULAR);
        
        when(repository.findById(requestId)).thenReturn(Optional.of(policyRequest));
        when(fraudAnalysisService.analyzeFraud(policyRequest)).thenReturn(riskAnalysis);
        when(repository.save(any(PolicyRequest.class))).thenReturn(policyRequest);

        policyRequestService.processFraudAnalysis(requestId);

        verify(fraudAnalysisService).analyzeFraud(policyRequest);
        verify(repository, times(2)).save(any(PolicyRequest.class));
    }

    @Test
    void testProcessFraudAnalysisFailure() {
        when(repository.findById(requestId)).thenReturn(Optional.of(policyRequest));
        when(fraudAnalysisService.analyzeFraud(policyRequest))
            .thenThrow(new RuntimeException("Analysis failed"));
        when(repository.save(any(PolicyRequest.class))).thenReturn(policyRequest);

        policyRequestService.processFraudAnalysis(requestId);

        verify(fraudAnalysisService).analyzeFraud(policyRequest);
        assertEquals(PolicyRequestStatus.REJECTED, policyRequest.getStatus());
    }

    @Test
    void testProcessPaymentSuccess() {
        when(repository.findById(requestId)).thenReturn(Optional.of(policyRequest));
        when(repository.save(any(PolicyRequest.class))).thenReturn(policyRequest);

        policyRequestService.processPayment(requestId);

        verify(paymentService).processPayment(policyRequest);
        verify(repository).save(any(PolicyRequest.class));
    }

    @Test
    void testProcessPaymentFailure() {
        when(repository.findById(requestId)).thenReturn(Optional.of(policyRequest));
        when(repository.save(any(PolicyRequest.class))).thenReturn(policyRequest);
        doThrow(new RuntimeException("Payment failed")).when(paymentService).processPayment(policyRequest);

        policyRequestService.processPayment(requestId);

        verify(paymentService).processPayment(policyRequest);
        verify(repository).save(any(PolicyRequest.class));
    }

    @Test
    void testProcessSubscriptionSuccess() {
        when(repository.findById(requestId)).thenReturn(Optional.of(policyRequest));
        when(repository.save(any(PolicyRequest.class))).thenReturn(policyRequest);

        policyRequestService.processSubscription(requestId);

        verify(subscriptionService).processSubscription(policyRequest);
        verify(repository).save(any(PolicyRequest.class));
    }

    @Test
    void testProcessSubscriptionFailure() {
        when(repository.findById(requestId)).thenReturn(Optional.of(policyRequest));
        when(repository.save(any(PolicyRequest.class))).thenReturn(policyRequest);
        doThrow(new RuntimeException("Subscription failed"))
            .when(subscriptionService).processSubscription(policyRequest);

        policyRequestService.processSubscription(requestId);

        verify(subscriptionService).processSubscription(policyRequest);
        verify(repository).save(any(PolicyRequest.class));
    }

    @Test
    void testCancelPolicyRequest() {
        policyRequest.setStatus(PolicyRequestStatus.VALIDATED);
        when(repository.findById(requestId)).thenReturn(Optional.of(policyRequest));
        when(repository.save(any(PolicyRequest.class))).thenReturn(policyRequest);

        policyRequestService.cancelPolicyRequest(requestId);

        assertEquals(PolicyRequestStatus.CANCELLED, policyRequest.getStatus());
        verify(eventPublisher).publish(
            eq(RabbitMQConfig.POLICY_EVENTS_EXCHANGE),
            eq("policy.cancelled"),
            any(PolicyRequestEvent.class)
        );
    }

    @Test
    void testCancelApprovedPolicyRequest() {
        policyRequest.setStatus(PolicyRequestStatus.APPROVED);
        when(repository.findById(requestId)).thenReturn(Optional.of(policyRequest));

        assertThrows(IllegalStateException.class, () ->
            policyRequestService.cancelPolicyRequest(requestId)
        );

        verify(repository, never()).save(any(PolicyRequest.class));
        verify(eventPublisher, never()).publish(any(), any(), any());
    }

    @Test
    void testValidatePolicyRequestWithoutRiskAnalysis() {
        when(repository.findById(requestId)).thenReturn(Optional.of(policyRequest));

        assertThrows(IllegalStateException.class, () ->
            policyRequestService.validatePolicyRequest(requestId)
        );
    }

    @Test
    void testValidatePolicyRequestWithValidRiskAnalysis() {
        RiskAnalysis riskAnalysis = new RiskAnalysis();
        riskAnalysis.setClassification(CustomerRiskType.REGULAR);
        policyRequest.setRiskAnalysis(riskAnalysis);
        
        when(repository.findById(requestId)).thenReturn(Optional.of(policyRequest));
        when(repository.save(any(PolicyRequest.class))).thenReturn(policyRequest);

        policyRequestService.validatePolicyRequest(requestId);

        verify(repository).save(any(PolicyRequest.class));
    }

    @Test
    void testValidatePolicyRequestWithHighRiskAnalysis() {
        RiskAnalysis riskAnalysis = new RiskAnalysis();
        riskAnalysis.setClassification(CustomerRiskType.HIGH_RISK);
        policyRequest.setRiskAnalysis(riskAnalysis);
        policyRequest.setInsuredAmount(BigDecimal.valueOf(500000.00));
        
        when(repository.findById(requestId)).thenReturn(Optional.of(policyRequest));
        when(repository.save(any(PolicyRequest.class))).thenReturn(policyRequest);

        policyRequestService.validatePolicyRequest(requestId);

        verify(repository).save(any(PolicyRequest.class));
    }
} 