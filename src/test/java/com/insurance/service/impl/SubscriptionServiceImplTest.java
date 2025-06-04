package com.insurance.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
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
} 