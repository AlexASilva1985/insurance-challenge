package com.insurance.infrastructure.messaging.service;

import com.insurance.infrastructure.messaging.config.RabbitMQConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class MessagingService {

    private final RabbitTemplate rabbitTemplate;

    public void sendFraudAnalysisRequest(Object message) {
        log.info("Sending fraud analysis request: {}", message);
        rabbitTemplate.convertAndSend(
            RabbitMQConfig.FRAUD_ANALYSIS_EXCHANGE,
            RabbitMQConfig.FRAUD_ANALYSIS_ROUTING_KEY,
            message
        );
    }

    public void sendPaymentRequest(Object message) {
        log.info("Sending payment request: {}", message);
        rabbitTemplate.convertAndSend(
            RabbitMQConfig.PAYMENT_EXCHANGE,
            RabbitMQConfig.PAYMENT_ROUTING_KEY,
            message
        );
    }
} 