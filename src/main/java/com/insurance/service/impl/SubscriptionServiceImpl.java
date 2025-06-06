package com.insurance.service.impl;

import com.insurance.domain.PolicyRequest;
import com.insurance.domain.enums.PolicyRequestStatus;
import com.insurance.event.SubscriptionApprovedEvent;
import com.insurance.infrastructure.messaging.config.RabbitMQConfig;
import com.insurance.infrastructure.messaging.service.EventPublisher;
import com.insurance.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriptionServiceImpl implements SubscriptionService {

    private final EventPublisher eventPublisher;

    @Override
    @Transactional
    public void processSubscription(PolicyRequest request) {
        validateRequest(request);

        log.info("Processing subscription for policy request: {}", request.getId());
        
        request.setStatus(PolicyRequestStatus.APPROVED);
        
        eventPublisher.publish(
            RabbitMQConfig.POLICY_EVENTS_EXCHANGE,
            RabbitMQConfig.POLICY_APPROVED_KEY,
            new SubscriptionApprovedEvent(request)
        );
    }

    private void validateRequest(PolicyRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Policy request cannot be null");
        }
        if (request.getId() == null) {
            throw new IllegalArgumentException("Policy request ID cannot be null");
        }
        if (request.getCustomerId() == null) {
            throw new IllegalArgumentException("Customer ID cannot be null");
        }
        if (request.getCategory() == null) {
            throw new IllegalArgumentException("Insurance category cannot be null");
        }
        if (request.getPaymentMethod() == null) {
            throw new IllegalArgumentException("Payment method cannot be null");
        }
        if (request.getInsuredAmount() == null) {
            throw new IllegalArgumentException("Insured amount cannot be null");
        }
        if (request.getInsuredAmount().signum() <= 0) {
            throw new IllegalArgumentException("Insured amount must be greater than zero");
        }
        if (request.getStatus() == null) {
            throw new IllegalArgumentException("Policy request status cannot be null");
        }
        if (request.getStatus() != PolicyRequestStatus.PENDING) {
            throw new IllegalStateException("Cannot process subscription for request in status: " + request.getStatus());
        }
    }
} 