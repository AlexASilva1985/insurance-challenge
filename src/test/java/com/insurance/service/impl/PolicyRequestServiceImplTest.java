package com.insurance.service.impl;

import com.insurance.domain.PolicyRequest;
import com.insurance.domain.RiskAnalysis;
import com.insurance.domain.enums.*;
import com.insurance.event.PolicyRequestCreatedEvent;
import com.insurance.event.PolicyRequestEvent;
import com.insurance.infrastructure.messaging.config.RabbitMQConfig;
import com.insurance.infrastructure.messaging.service.EventPublisher;
import com.insurance.repository.PolicyRequestRepository;
import com.insurance.service.FraudAnalysisService;
import com.insurance.service.PaymentService;
import com.insurance.service.SubscriptionService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

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
        // Arrange
        policyRequest.setStatus(PolicyRequestStatus.VALIDATED);
        policyRequest.setPaymentMethod(PaymentMethod.CREDIT_CARD);

        when(repository.findById(requestId)).thenReturn(Optional.of(policyRequest));
        when(repository.save(any(PolicyRequest.class))).thenAnswer(invocation -> {
            PolicyRequest savedRequest = invocation.getArgument(0);
            policyRequest.setStatus(savedRequest.getStatus());
            return policyRequest;
        });
        when(paymentService.processPayment(policyRequest)).thenReturn(true);

        // Act
        policyRequestService.processPayment(requestId);

        // Assert
        verify(paymentService).processPayment(policyRequest);
        verify(repository).save(any(PolicyRequest.class));
        verify(eventPublisher).publish(
            eq(RabbitMQConfig.POLICY_EVENTS_EXCHANGE),
            eq(RabbitMQConfig.PAYMENT_PROCESSED_KEY),
            eventCaptor.capture()
        );

        PolicyRequestEvent event = eventCaptor.getValue();
        assertEquals(requestId, event.getPolicyRequestId());
        assertEquals(customerId, event.getCustomerId());
        assertEquals(PolicyRequestStatus.PENDING, event.getStatus());
        assertEquals(PolicyRequestStatus.PENDING, policyRequest.getStatus());
    }

    @Test
    void testProcessPaymentFailure() {
        // Arrange
        policyRequest.setStatus(PolicyRequestStatus.VALIDATED);
        policyRequest.setPaymentMethod(PaymentMethod.CREDIT_CARD);

        when(repository.findById(requestId)).thenReturn(Optional.of(policyRequest));
        when(repository.save(any(PolicyRequest.class))).thenAnswer(invocation -> {
            PolicyRequest savedRequest = invocation.getArgument(0);
            policyRequest.setStatus(savedRequest.getStatus());
            return policyRequest;
        });
        when(paymentService.processPayment(policyRequest)).thenReturn(false);

        // Act
        policyRequestService.processPayment(requestId);

        // Assert
        verify(paymentService).processPayment(policyRequest);
        verify(repository).save(any(PolicyRequest.class));
        verify(eventPublisher).publish(
            eq(RabbitMQConfig.POLICY_EVENTS_EXCHANGE),
            eq(RabbitMQConfig.PAYMENT_REJECTED_KEY),
            eventCaptor.capture()
        );

        PolicyRequestEvent event = eventCaptor.getValue();
        assertEquals(requestId, event.getPolicyRequestId());
        assertEquals(customerId, event.getCustomerId());
        assertEquals(PolicyRequestStatus.REJECTED, event.getStatus());
        assertEquals(PolicyRequestStatus.REJECTED, policyRequest.getStatus());
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

    @Test
    void testValidateRegularCustomerWithDifferentCategories() {
        RiskAnalysis riskAnalysis = new RiskAnalysis();
        riskAnalysis.setClassification(CustomerRiskType.REGULAR);
        policyRequest.setRiskAnalysis(riskAnalysis);
        
        when(repository.findById(requestId)).thenReturn(Optional.of(policyRequest));
        when(repository.save(any(PolicyRequest.class))).thenReturn(policyRequest);

        for (InsuranceCategory category : InsuranceCategory.values()) {
            policyRequest.setCategory(category);
            policyRequest.setInsuredAmount(new BigDecimal("500000.00"));
            policyRequest.setStatus(PolicyRequestStatus.RECEIVED);

            policyRequestService.validatePolicyRequest(requestId);
        }

        // Verify calls based on number of categories
        verify(repository, times(InsuranceCategory.values().length)).save(policyRequest);
        verify(eventPublisher, times(InsuranceCategory.values().length)).publish(
            eq(RabbitMQConfig.POLICY_EVENTS_EXCHANGE),
            eq(RabbitMQConfig.POLICY_VALIDATED_KEY),
            eventCaptor.capture()
        );

        // Check the last captured event
        PolicyRequestEvent event = eventCaptor.getValue();
        assertEquals(requestId, event.getPolicyRequestId());
        assertEquals(customerId, event.getCustomerId());
        assertEquals(PolicyRequestStatus.VALIDATED, event.getStatus());
    }

    @Test
    void testValidateHighRiskCustomerWithDifferentCategories() {
        RiskAnalysis riskAnalysis = new RiskAnalysis();
        riskAnalysis.setClassification(CustomerRiskType.HIGH_RISK);
        policyRequest.setRiskAnalysis(riskAnalysis);
        
        when(repository.findById(requestId)).thenReturn(Optional.of(policyRequest));
        when(repository.save(any(PolicyRequest.class))).thenReturn(policyRequest);

        for (InsuranceCategory category : InsuranceCategory.values()) {
            policyRequest.setCategory(category);
            policyRequest.setInsuredAmount(new BigDecimal("100000.00"));
            policyRequest.setStatus(PolicyRequestStatus.RECEIVED);

            policyRequestService.validatePolicyRequest(requestId);
        }

        // Verify calls based on number of categories
        verify(repository, times(InsuranceCategory.values().length)).save(policyRequest);
        verify(eventPublisher, times(InsuranceCategory.values().length)).publish(
            eq(RabbitMQConfig.POLICY_EVENTS_EXCHANGE),
            eq(RabbitMQConfig.POLICY_REJECTED_KEY),
            eventCaptor.capture()
        );

        // Check the last captured event
        PolicyRequestEvent event = eventCaptor.getValue();
        assertEquals(requestId, event.getPolicyRequestId());
        assertEquals(customerId, event.getCustomerId());
        assertEquals(PolicyRequestStatus.REJECTED, event.getStatus());
    }

    @Test
    void testValidatePreferredCustomerWithDifferentCategories() {
        RiskAnalysis riskAnalysis = new RiskAnalysis();
        riskAnalysis.setClassification(CustomerRiskType.PREFERRED);
        policyRequest.setRiskAnalysis(riskAnalysis);
        
        when(repository.findById(requestId)).thenReturn(Optional.of(policyRequest));
        when(repository.save(any(PolicyRequest.class))).thenReturn(policyRequest);

        for (InsuranceCategory category : InsuranceCategory.values()) {
            policyRequest.setCategory(category);
            policyRequest.setInsuredAmount(new BigDecimal("1000000.00"));
            policyRequest.setStatus(PolicyRequestStatus.RECEIVED);

            policyRequestService.validatePolicyRequest(requestId);
        }

        // Verify calls based on number of categories
        verify(repository, times(InsuranceCategory.values().length)).save(policyRequest);
        verify(eventPublisher, times(InsuranceCategory.values().length)).publish(
            eq(RabbitMQConfig.POLICY_EVENTS_EXCHANGE),
            eq(RabbitMQConfig.POLICY_VALIDATED_KEY),
            eventCaptor.capture()
        );

        // Check the last captured event
        PolicyRequestEvent event = eventCaptor.getValue();
        assertEquals(requestId, event.getPolicyRequestId());
        assertEquals(customerId, event.getCustomerId());
        assertEquals(PolicyRequestStatus.VALIDATED, event.getStatus());
    }

    @Test
    void testValidateNoInformationCustomerWithDifferentCategories() {
        RiskAnalysis riskAnalysis = new RiskAnalysis();
        riskAnalysis.setClassification(CustomerRiskType.NO_INFORMATION);
        
        // Test LIFE insurance within limit
        policyRequest.setCategory(InsuranceCategory.LIFE);
        policyRequest.setInsuredAmount(new BigDecimal("100000.00"));
        policyRequest.setRiskAnalysis(riskAnalysis);
        
        when(repository.findById(requestId)).thenReturn(Optional.of(policyRequest));
        when(repository.save(any(PolicyRequest.class))).thenReturn(policyRequest);

        policyRequestService.validatePolicyRequest(requestId);
        assertEquals(PolicyRequestStatus.VALIDATED, policyRequest.getStatus());

        // Test other category above limit
        policyRequest.setCategory(InsuranceCategory.TRAVEL);
        policyRequest.setInsuredAmount(new BigDecimal("75000.00"));
        
        policyRequestService.validatePolicyRequest(requestId);
        assertEquals(PolicyRequestStatus.REJECTED, policyRequest.getStatus());
    }

    @Test
    void testStatusTransitionValidations() {
        when(repository.findById(requestId)).thenReturn(Optional.of(policyRequest));
        when(repository.save(any(PolicyRequest.class))).thenReturn(policyRequest);

        // Test valid transitions
        policyRequest.setStatus(PolicyRequestStatus.RECEIVED);
        policyRequestService.updateStatus(requestId, PolicyRequestStatus.VALIDATED);
        assertEquals(PolicyRequestStatus.VALIDATED, policyRequest.getStatus());

        policyRequest.setStatus(PolicyRequestStatus.VALIDATED);
        policyRequestService.updateStatus(requestId, PolicyRequestStatus.PENDING);
        assertEquals(PolicyRequestStatus.PENDING, policyRequest.getStatus());

        policyRequest.setStatus(PolicyRequestStatus.PENDING);
        policyRequestService.updateStatus(requestId, PolicyRequestStatus.APPROVED);
        assertEquals(PolicyRequestStatus.APPROVED, policyRequest.getStatus());

        // Test invalid transitions
        policyRequest.setStatus(PolicyRequestStatus.REJECTED);
        assertThrows(IllegalStateException.class, () ->
            policyRequestService.updateStatus(requestId, PolicyRequestStatus.VALIDATED)
        );

        policyRequest.setStatus(PolicyRequestStatus.APPROVED);
        assertThrows(IllegalStateException.class, () ->
            policyRequestService.updateStatus(requestId, PolicyRequestStatus.PENDING)
        );

        policyRequest.setStatus(PolicyRequestStatus.CANCELLED);
        assertThrows(IllegalStateException.class, () ->
            policyRequestService.updateStatus(requestId, PolicyRequestStatus.VALIDATED)
        );
    }
} 