package com.insurance.infrastructure.messaging.service;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import com.insurance.domain.enums.PolicyRequestStatus;
import com.insurance.event.PolicyRequestEvent;
import com.insurance.infrastructure.messaging.config.RabbitMQConfig;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

@ExtendWith(MockitoExtension.class)
class EventPublisherTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private EventPublisher eventPublisher;

    private PolicyRequestEvent testEvent;
    private String testExchange;
    private String testRoutingKey;

    // Classe de teste para criar um evento concreto
    private static class TestPolicyRequestEvent extends PolicyRequestEvent {
        public TestPolicyRequestEvent(UUID policyRequestId, UUID customerId, PolicyRequestStatus status) {
            super(policyRequestId, customerId, status);
        }
    }

    @BeforeEach
    void setUp() {
        UUID policyRequestId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        testEvent = new TestPolicyRequestEvent(policyRequestId, customerId, PolicyRequestStatus.RECEIVED);
        testExchange = RabbitMQConfig.POLICY_EVENTS_EXCHANGE;
        testRoutingKey = RabbitMQConfig.POLICY_CREATED_KEY;
    }

    @Test
    void testPublishEvent() {
        eventPublisher.publish(testExchange, testRoutingKey, testEvent);

        verify(rabbitTemplate).convertAndSend(
            eq(testExchange),
            eq(testRoutingKey),
            eq(testEvent)
        );
    }

    @Test
    void testPublishEventWithDifferentExchangeAndRoutingKey() {
        String fraudExchange = RabbitMQConfig.FRAUD_ANALYSIS_EXCHANGE;
        String fraudRoutingKey = RabbitMQConfig.FRAUD_ANALYSIS_ROUTING_KEY;

        eventPublisher.publish(fraudExchange, fraudRoutingKey, testEvent);

        verify(rabbitTemplate).convertAndSend(
            eq(fraudExchange),
            eq(fraudRoutingKey),
            eq(testEvent)
        );
    }

    @Test
    void testPublishEventWithPaymentExchange() {
        String paymentExchange = RabbitMQConfig.PAYMENT_EXCHANGE;
        String paymentRoutingKey = RabbitMQConfig.PAYMENT_ROUTING_KEY;

        eventPublisher.publish(paymentExchange, paymentRoutingKey, testEvent);

        verify(rabbitTemplate).convertAndSend(
            eq(paymentExchange),
            eq(paymentRoutingKey),
            eq(testEvent)
        );
    }

    @Test
    void testPublishEventWithDifferentStatus() {
        UUID policyRequestId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        PolicyRequestEvent validatedEvent = new TestPolicyRequestEvent(
            policyRequestId, 
            customerId, 
            PolicyRequestStatus.VALIDATED
        );

        eventPublisher.publish(testExchange, RabbitMQConfig.POLICY_VALIDATED_KEY, validatedEvent);

        verify(rabbitTemplate).convertAndSend(
            eq(testExchange),
            eq(RabbitMQConfig.POLICY_VALIDATED_KEY),
            eq(validatedEvent)
        );
    }
} 