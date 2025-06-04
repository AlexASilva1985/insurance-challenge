package com.insurance.service.impl;

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
class PaymentServiceImplTest {

    @Mock
    private EventPublisher eventPublisher;

    @InjectMocks
    private PaymentServiceImpl paymentService;

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
        policyRequest.setStatus(PolicyRequestStatus.VALIDATED);
        policyRequest.setCreatedAt(now);
        policyRequest.setUpdatedAt(now);
        policyRequest.setCreatedBy("system");
        policyRequest.setUpdatedBy("system");
        policyRequest.setTotalMonthlyPremiumAmount(BigDecimal.valueOf(150.00));
        policyRequest.setInsuredAmount(BigDecimal.valueOf(50000.00));
    }

    @Test
    void testProcessPayment() {
        paymentService.processPayment(policyRequest);

        verify(eventPublisher).publish(
            eq(RabbitMQConfig.POLICY_EVENTS_EXCHANGE),
            eq(RabbitMQConfig.PAYMENT_REQUESTED_KEY),
            eventCaptor.capture()
        );

        PolicyRequestEvent capturedEvent = eventCaptor.getValue();
        org.junit.jupiter.api.Assertions.assertEquals(requestId, capturedEvent.getPolicyRequestId());
        org.junit.jupiter.api.Assertions.assertEquals(customerId, capturedEvent.getCustomerId());
        org.junit.jupiter.api.Assertions.assertEquals(PolicyRequestStatus.VALIDATED, capturedEvent.getStatus());
    }

    @Test
    void testProcessPaymentWithDifferentStatus() {
        policyRequest.setStatus(PolicyRequestStatus.RECEIVED);
        
        paymentService.processPayment(policyRequest);

        verify(eventPublisher).publish(
            eq(RabbitMQConfig.POLICY_EVENTS_EXCHANGE),
            eq(RabbitMQConfig.PAYMENT_REQUESTED_KEY),
            eventCaptor.capture()
        );

        PolicyRequestEvent capturedEvent = eventCaptor.getValue();
        org.junit.jupiter.api.Assertions.assertEquals(requestId, capturedEvent.getPolicyRequestId());
        org.junit.jupiter.api.Assertions.assertEquals(customerId, capturedEvent.getCustomerId());
        org.junit.jupiter.api.Assertions.assertEquals(PolicyRequestStatus.RECEIVED, capturedEvent.getStatus());
    }

    @Test
    void testProcessPaymentWithDifferentPaymentMethod() {
        policyRequest.setPaymentMethod(PaymentMethod.BANK_TRANSFER);
        
        paymentService.processPayment(policyRequest);

        verify(eventPublisher).publish(
            eq(RabbitMQConfig.POLICY_EVENTS_EXCHANGE),
            eq(RabbitMQConfig.PAYMENT_REQUESTED_KEY),
            eventCaptor.capture()
        );

        PolicyRequestEvent capturedEvent = eventCaptor.getValue();
        org.junit.jupiter.api.Assertions.assertEquals(requestId, capturedEvent.getPolicyRequestId());
        org.junit.jupiter.api.Assertions.assertEquals(customerId, capturedEvent.getCustomerId());
        org.junit.jupiter.api.Assertions.assertEquals(PolicyRequestStatus.VALIDATED, capturedEvent.getStatus());
    }
} 