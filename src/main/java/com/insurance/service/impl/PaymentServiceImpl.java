package com.insurance.service.impl;

import com.insurance.domain.PolicyRequest;
import com.insurance.event.PolicyRequestEvent;
import com.insurance.infrastructure.messaging.config.RabbitMQConfig;
import com.insurance.infrastructure.messaging.service.EventPublisher;
import com.insurance.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final EventPublisher eventPublisher;

    @Override
    @Transactional
    public void processPayment(PolicyRequest request) {
        log.info("Processing payment for policy request: {}", request.getId());
        
        eventPublisher.publish(
            RabbitMQConfig.POLICY_EVENTS_EXCHANGE,
            RabbitMQConfig.PAYMENT_REQUESTED_KEY,
            new PolicyRequestEvent(request.getId(), request.getCustomerId(), request.getStatus()) {}
        );
    }
} 