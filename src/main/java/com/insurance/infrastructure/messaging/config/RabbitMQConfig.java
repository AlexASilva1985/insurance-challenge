package com.insurance.infrastructure.messaging.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // Event exchanges
    public static final String POLICY_EVENTS_EXCHANGE = "policy.events.exchange";
    public static final String FRAUD_ANALYSIS_EXCHANGE = "fraud.analysis.exchange";
    public static final String PAYMENT_EXCHANGE = "payment.exchange";
    
    // Event routing keys
    public static final String POLICY_CREATED_KEY = "policy.created";
    public static final String FRAUD_ANALYSIS_REQUESTED_KEY = "fraud.analysis.requested";
    public static final String FRAUD_ANALYSIS_COMPLETED_KEY = "fraud.analysis.completed";
    public static final String FRAUD_ANALYSIS_ROUTING_KEY = "fraud.analysis.request";
    public static final String PAYMENT_REQUESTED_KEY = "payment.requested";
    public static final String PAYMENT_COMPLETED_KEY = "payment.completed";
    public static final String PAYMENT_ROUTING_KEY = "payment.request";
    public static final String POLICY_VALIDATED_KEY = "policy.validated";
    public static final String POLICY_REJECTED_KEY = "policy.rejected";
    public static final String POLICY_APPROVED_KEY = "policy.approved";
    
    // Event queues
    public static final String FRAUD_ANALYSIS_QUEUE = "fraud.analysis.queue";
    public static final String PAYMENT_QUEUE = "payment.queue";
    public static final String POLICY_STATUS_QUEUE = "policy.status.queue";

    @Bean
    public TopicExchange policyEventsExchange() {
        return new TopicExchange(POLICY_EVENTS_EXCHANGE);
    }

    @Bean
    public TopicExchange fraudAnalysisExchange() {
        return new TopicExchange(FRAUD_ANALYSIS_EXCHANGE);
    }

    @Bean
    public TopicExchange paymentExchange() {
        return new TopicExchange(PAYMENT_EXCHANGE);
    }

    @Bean
    public Queue fraudAnalysisQueue() {
        return new Queue(FRAUD_ANALYSIS_QUEUE, true);
    }

    @Bean
    public Queue paymentQueue() {
        return new Queue(PAYMENT_QUEUE, true);
    }

    @Bean
    public Queue policyStatusQueue() {
        return new Queue(POLICY_STATUS_QUEUE, true);
    }

    @Bean
    public Binding fraudAnalysisBinding(Queue fraudAnalysisQueue, TopicExchange fraudAnalysisExchange) {
        return BindingBuilder
                .bind(fraudAnalysisQueue)
                .to(fraudAnalysisExchange)
                .with("fraud.analysis.*");
    }

    @Bean
    public Binding paymentBinding(Queue paymentQueue, TopicExchange paymentExchange) {
        return BindingBuilder
                .bind(paymentQueue)
                .to(paymentExchange)
                .with("payment.*");
    }

    @Bean
    public Binding policyStatusBinding(Queue policyStatusQueue, TopicExchange policyEventsExchange) {
        return BindingBuilder
                .bind(policyStatusQueue)
                .to(policyEventsExchange)
                .with("policy.*");
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public AmqpTemplate amqpTemplate(ConnectionFactory connectionFactory) {
        final RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        return rabbitTemplate;
    }
} 