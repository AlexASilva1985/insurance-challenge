package com.insurance.service.impl;

import com.insurance.domain.PolicyRequest;
import com.insurance.domain.enums.PolicyRequestStatus;
import com.insurance.event.PolicyRequestEvent;
import com.insurance.infrastructure.messaging.config.RabbitMQConfig;
import com.insurance.infrastructure.messaging.service.EventPublisher;
import com.insurance.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriptionServiceImpl implements SubscriptionService {

    private final EventPublisher eventPublisher;

    @Override
    public void processSubscription(PolicyRequest request) {
        log.info("Processing subscription for policy request: {}", request.getId());
        
        if (request.getStatus() != PolicyRequestStatus.PENDING) {
            throw new IllegalStateException("Cannot process subscription for request in status: " + request.getStatus());
        }
        
        eventPublisher.publish(
            RabbitMQConfig.POLICY_EVENTS_EXCHANGE,
            RabbitMQConfig.POLICY_APPROVED_KEY,
            new PolicyRequestEvent(request.getId(), request.getCustomerId(), PolicyRequestStatus.APPROVED) {}
        );
    }
} 