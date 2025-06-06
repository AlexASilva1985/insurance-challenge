package com.insurance.infrastructure.messaging.config;

import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String POLICY_EVENTS_EXCHANGE = "policy.events.exchange";
    public static final String FRAUD_ANALYSIS_EXCHANGE = "fraud.analysis.exchange";
    public static final String PAYMENT_EXCHANGE = "payment.exchange";
    
    public static final String POLICY_CREATED_KEY = "policy.created";
    public static final String FRAUD_ANALYSIS_ROUTING_KEY = "fraud.analysis.request";
    public static final String PAYMENT_REQUESTED_KEY = "payment.requested";
    public static final String PAYMENT_ROUTING_KEY = "payment.request";
    public static final String POLICY_VALIDATED_KEY = "policy.validated";
    public static final String POLICY_REJECTED_KEY = "policy.rejected";
    public static final String POLICY_APPROVED_KEY = "policy.approved";
    public static final String PAYMENT_PROCESSED_KEY = "payment.processed";
    public static final String PAYMENT_REJECTED_KEY = "payment.rejected";

    @Bean
    public TopicExchange policyEventsExchange() {
        return new TopicExchange(POLICY_EVENTS_EXCHANGE);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

} 