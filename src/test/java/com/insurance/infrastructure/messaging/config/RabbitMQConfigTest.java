package com.insurance.infrastructure.messaging.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;

class RabbitMQConfigTest {

    private final RabbitMQConfig config = new RabbitMQConfig();

    @Test
    void testPolicyEventsExchange() {
        TopicExchange exchange = config.policyEventsExchange();
        
        assertNotNull(exchange);
        assertEquals(RabbitMQConfig.POLICY_EVENTS_EXCHANGE, exchange.getName());
        assertTrue(exchange.isDurable());
        assertFalse(exchange.isAutoDelete());
    }

    @Test
    void testJsonMessageConverter() {
        MessageConverter converter = config.jsonMessageConverter();
        
        assertNotNull(converter);
        assertTrue(converter instanceof Jackson2JsonMessageConverter);
    }

    @Test
    void testExchangeConstants() {
        assertEquals("policy.events.exchange", RabbitMQConfig.POLICY_EVENTS_EXCHANGE);
        assertEquals("fraud.analysis.exchange", RabbitMQConfig.FRAUD_ANALYSIS_EXCHANGE);
        assertEquals("payment.exchange", RabbitMQConfig.PAYMENT_EXCHANGE);
    }

    @Test
    void testRoutingKeyConstants() {
        assertEquals("policy.created", RabbitMQConfig.POLICY_CREATED_KEY);
        assertEquals("fraud.analysis.request", RabbitMQConfig.FRAUD_ANALYSIS_ROUTING_KEY);
        assertEquals("payment.requested", RabbitMQConfig.PAYMENT_REQUESTED_KEY);
        assertEquals("payment.request", RabbitMQConfig.PAYMENT_ROUTING_KEY);
        assertEquals("policy.validated", RabbitMQConfig.POLICY_VALIDATED_KEY);
        assertEquals("policy.rejected", RabbitMQConfig.POLICY_REJECTED_KEY);
        assertEquals("policy.approved", RabbitMQConfig.POLICY_APPROVED_KEY);
    }
} 