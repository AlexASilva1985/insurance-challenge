package com.insurance.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.mock;

import com.insurance.domain.PolicyRequest;
import com.insurance.domain.enums.InsuranceCategory;
import com.insurance.domain.enums.PaymentMethod;
import com.insurance.domain.enums.PolicyRequestStatus;
import com.insurance.domain.enums.SalesChannel;
import com.insurance.event.PolicyRequestEvent;
import com.insurance.infrastructure.messaging.config.RabbitMQConfig;
import com.insurance.infrastructure.messaging.service.EventPublisher;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SubscriptionServiceImplTest {

    @Mock
    private EventPublisher eventPublisher;

    @InjectMocks
    private SubscriptionServiceImpl subscriptionService;

    @Captor
    private ArgumentCaptor<PolicyRequestEvent> eventCaptor;

    private PolicyRequest policyRequest;
    private UUID requestId;
    private UUID customerId;

    @BeforeEach
    void setUp() {
        requestId = UUID.randomUUID();
        customerId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        policyRequest = new PolicyRequest();
        policyRequest.setId(requestId);
        policyRequest.setCustomerId(customerId);
        policyRequest.setProductId(UUID.randomUUID());
        policyRequest.setCategory(InsuranceCategory.AUTO);
        policyRequest.setSalesChannel(SalesChannel.MOBILE);
        policyRequest.setPaymentMethod(PaymentMethod.CREDIT_CARD);
        policyRequest.setStatus(PolicyRequestStatus.PENDING);
        policyRequest.setCreatedAt(now);
        policyRequest.setUpdatedAt(now);
        policyRequest.setCreatedBy("system");
        policyRequest.setUpdatedBy("system");
        policyRequest.setTotalMonthlyPremiumAmount(BigDecimal.valueOf(150.00));
        policyRequest.setInsuredAmount(BigDecimal.valueOf(50000.00));
    }

    @Test
    void testProcessSubscriptionSuccess() {
        subscriptionService.processSubscription(policyRequest);

        verify(eventPublisher).publish(
            eq(RabbitMQConfig.POLICY_EVENTS_EXCHANGE),
            eq(RabbitMQConfig.POLICY_APPROVED_KEY),
            eventCaptor.capture()
        );

        PolicyRequestEvent capturedEvent = eventCaptor.getValue();
        assertEquals(requestId, capturedEvent.getPolicyRequestId());
        assertEquals(customerId, capturedEvent.getCustomerId());
        assertEquals(PolicyRequestStatus.APPROVED, capturedEvent.getStatus());
    }

    @Test
    void testProcessSubscriptionWithInvalidStatus() {
        policyRequest.setStatus(PolicyRequestStatus.RECEIVED);

        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
            subscriptionService.processSubscription(policyRequest)
        );

        assertEquals(
            "Cannot process subscription for request in status: RECEIVED",
            exception.getMessage()
        );
    }

    @Test
    void testProcessSubscriptionWithValidatedStatus() {
        policyRequest.setStatus(PolicyRequestStatus.VALIDATED);

        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
            subscriptionService.processSubscription(policyRequest)
        );

        assertEquals(
            "Cannot process subscription for request in status: VALIDATED",
            exception.getMessage()
        );
    }

    @Test
    void testProcessSubscriptionWithApprovedStatus() {
        policyRequest.setStatus(PolicyRequestStatus.APPROVED);

        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
            subscriptionService.processSubscription(policyRequest)
        );

        assertEquals(
            "Cannot process subscription for request in status: APPROVED",
            exception.getMessage()
        );
    }

    @Test
    void testProcessSubscriptionWithRejectedStatus() {
        policyRequest.setStatus(PolicyRequestStatus.REJECTED);

        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
            subscriptionService.processSubscription(policyRequest)
        );

        assertEquals(
            "Cannot process subscription for request in status: REJECTED",
            exception.getMessage()
        );
    }

    @Test
    void testProcessSubscriptionWithCancelledStatus() {
        policyRequest.setStatus(PolicyRequestStatus.CANCELLED);

        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
            subscriptionService.processSubscription(policyRequest)
        );

        assertEquals(
            "Cannot process subscription for request in status: CANCELLED",
            exception.getMessage()
        );
    }

    @Test
    void testProcessSubscriptionWithEventPublisherError() {
        doThrow(new RuntimeException("Failed to publish event"))
            .when(eventPublisher)
            .publish(any(), any(), any());

        assertThrows(RuntimeException.class, () ->
            subscriptionService.processSubscription(policyRequest)
        );

        verify(eventPublisher).publish(
            eq(RabbitMQConfig.POLICY_EVENTS_EXCHANGE),
            eq(RabbitMQConfig.POLICY_APPROVED_KEY),
            eventCaptor.capture()
        );
    }

    @Test
    void testProcessSubscriptionWithNullRequest() {
        assertThrows(IllegalArgumentException.class, () -> {
            subscriptionService.processSubscription(null);
        });

        verify(eventPublisher, never()).publish(any(), any(), any());
    }

    @Test
    void testProcessSubscriptionWithNullRequestId() {
        policyRequest.setId(null);
        
        assertThrows(IllegalArgumentException.class, () -> {
            subscriptionService.processSubscription(policyRequest);
        });

        verify(eventPublisher, never()).publish(any(), any(), any());
    }

    @Test
    void testProcessSubscriptionWithNullCustomerId() {
        policyRequest.setCustomerId(null);
        
        assertThrows(IllegalArgumentException.class, () -> {
            subscriptionService.processSubscription(policyRequest);
        });

        verify(eventPublisher, never()).publish(any(), any(), any());
    }

    @Test
    void testProcessSubscriptionWithNullCategory() {
        policyRequest.setCategory(null);
        
        assertThrows(IllegalArgumentException.class, () -> {
            subscriptionService.processSubscription(policyRequest);
        });

        verify(eventPublisher, never()).publish(any(), any(), any());
    }

    @Test
    void testProcessSubscriptionWithNullPaymentMethod() {
        policyRequest.setPaymentMethod(null);
        
        assertThrows(IllegalArgumentException.class, () -> {
            subscriptionService.processSubscription(policyRequest);
        });

        verify(eventPublisher, never()).publish(any(), any(), any());
    }

    @Test
    void testProcessSubscriptionWithZeroInsuredAmount() {
        PolicyRequest mockRequest = mock(PolicyRequest.class);
        when(mockRequest.getId()).thenReturn(requestId);
        when(mockRequest.getCustomerId()).thenReturn(customerId);
        when(mockRequest.getCategory()).thenReturn(InsuranceCategory.AUTO);
        when(mockRequest.getPaymentMethod()).thenReturn(PaymentMethod.CREDIT_CARD);
        when(mockRequest.getStatus()).thenReturn(PolicyRequestStatus.PENDING);
        when(mockRequest.getInsuredAmount()).thenReturn(BigDecimal.ZERO);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            subscriptionService.processSubscription(mockRequest)
        );

        assertEquals("Insured amount must be greater than zero", exception.getMessage());
        verify(eventPublisher, never()).publish(any(), any(), any());
    }

    @Test
    void testProcessSubscriptionWithNegativeInsuredAmount() {
        PolicyRequest mockRequest = mock(PolicyRequest.class);
        when(mockRequest.getId()).thenReturn(requestId);
        when(mockRequest.getCustomerId()).thenReturn(customerId);
        when(mockRequest.getCategory()).thenReturn(InsuranceCategory.AUTO);
        when(mockRequest.getPaymentMethod()).thenReturn(PaymentMethod.CREDIT_CARD);
        when(mockRequest.getStatus()).thenReturn(PolicyRequestStatus.PENDING);
        when(mockRequest.getInsuredAmount()).thenReturn(new BigDecimal("-50000.00"));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            subscriptionService.processSubscription(mockRequest)
        );

        assertEquals("Insured amount must be greater than zero", exception.getMessage());
        verify(eventPublisher, never()).publish(any(), any(), any());
    }

    @Test
    void testProcessSubscriptionWithAllCategories() {
        for (InsuranceCategory category : InsuranceCategory.values()) {
            policyRequest.setCategory(category);
            policyRequest.setStatus(PolicyRequestStatus.PENDING);
            subscriptionService.processSubscription(policyRequest);
        }

        verify(eventPublisher, times(InsuranceCategory.values().length))
            .publish(eq(RabbitMQConfig.POLICY_EVENTS_EXCHANGE), 
                    eq(RabbitMQConfig.POLICY_APPROVED_KEY), 
                    eventCaptor.capture());

        List<PolicyRequestEvent> events = eventCaptor.getAllValues();
        assertEquals(InsuranceCategory.values().length, events.size());
        
        for (int i = 0; i < events.size(); i++) {
            assertEquals(requestId, events.get(i).getPolicyRequestId());
            assertEquals(customerId, events.get(i).getCustomerId());
        }
    }

    @Test
    void testProcessSubscriptionWithAllPaymentMethods() {
        for (PaymentMethod method : PaymentMethod.values()) {
            policyRequest.setPaymentMethod(method);
            policyRequest.setStatus(PolicyRequestStatus.PENDING);
            subscriptionService.processSubscription(policyRequest);
        }

        verify(eventPublisher, times(PaymentMethod.values().length))
            .publish(eq(RabbitMQConfig.POLICY_EVENTS_EXCHANGE), 
                    eq(RabbitMQConfig.POLICY_APPROVED_KEY), 
                    eventCaptor.capture());

        List<PolicyRequestEvent> events = eventCaptor.getAllValues();
        assertEquals(PaymentMethod.values().length, events.size());
        
        for (int i = 0; i < events.size(); i++) {
            assertEquals(requestId, events.get(i).getPolicyRequestId());
            assertEquals(customerId, events.get(i).getCustomerId());
        }
    }
} 