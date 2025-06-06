package com.insurance.service.impl;

import com.insurance.domain.PolicyRequest;
import com.insurance.domain.RiskAnalysis;
import com.insurance.domain.enums.CustomerRiskType;
import com.insurance.domain.enums.InsuranceCategory;
import com.insurance.domain.enums.PolicyRequestStatus;
import com.insurance.event.*;
import com.insurance.infrastructure.messaging.config.RabbitMQConfig;
import com.insurance.infrastructure.messaging.service.EventPublisher;
import com.insurance.repository.PolicyRequestRepository;
import com.insurance.service.FraudAnalysisService;
import com.insurance.service.PaymentService;
import com.insurance.service.PolicyRequestService;
import com.insurance.service.SubscriptionService;
import jakarta.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PolicyRequestServiceImpl implements PolicyRequestService {

    private final PolicyRequestRepository repository;
    private final FraudAnalysisService fraudAnalysisService;
    private final PaymentService paymentService;
    private final SubscriptionService subscriptionService;
    private final EventPublisher eventPublisher;

    @Override
    @Transactional
    public PolicyRequest createPolicyRequest(PolicyRequest request) {
        request.setStatus(PolicyRequestStatus.RECEIVED);
        request = repository.save(request);
        
        eventPublisher.publish(
            RabbitMQConfig.POLICY_EVENTS_EXCHANGE,
            RabbitMQConfig.POLICY_CREATED_KEY,
            new PolicyRequestCreatedEvent(request)
        );
        
        return request;
    }

    @Override
    public PolicyRequest findById(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Policy request not found with id: " + id));
    }

    @Override
    public List<PolicyRequest> findByCustomerId(UUID customerId) {
        return repository.findByCustomerId(customerId);
    }

    @Override
    @Transactional
    public PolicyRequest updateStatus(UUID id, PolicyRequestStatus newStatus) {
        PolicyRequest request = findById(id);
        
        if (!request.canTransitionTo(newStatus)) {
            throw new IllegalStateException("Cannot transition from " + request.getStatus() + " to " + newStatus);
        }
        
        request.updateStatus(newStatus);
        request = repository.save(request);
        
        PolicyRequestEvent event = switch (newStatus) {
            case VALIDATED -> new PolicyValidatedEvent(request);
            case REJECTED -> new PolicyRejectedEvent(request);
            case APPROVED -> new SubscriptionApprovedEvent(request);
            case CANCELLED -> new PolicyCancelledEvent(request);
            case PENDING -> new PaymentProcessedEvent(request);
            default -> new PolicyStatusChangedEvent(request);
        };
        
        String routingKey = switch (newStatus) {
            case VALIDATED -> RabbitMQConfig.POLICY_VALIDATED_KEY;
            case REJECTED -> RabbitMQConfig.POLICY_REJECTED_KEY;
            case APPROVED -> RabbitMQConfig.POLICY_APPROVED_KEY;
            case CANCELLED -> "policy.cancelled";
            case PENDING -> RabbitMQConfig.PAYMENT_PROCESSED_KEY;
            default -> "policy.status.changed";
        };
        
        eventPublisher.publish(
            RabbitMQConfig.POLICY_EVENTS_EXCHANGE,
            routingKey,
            event
        );
        
        return request;
    }

    @Override
    @Transactional
    public void validatePolicyRequest(UUID id) {
        PolicyRequest request = findById(id);
        
        if (request.getRiskAnalysis() == null) {
            throw new IllegalStateException("Cannot validate policy request without risk analysis");
        }
        
        CustomerRiskType riskType = request.getRiskAnalysis().getClassification();
        boolean isValid = validateInsuranceAmount(request.getCategory(), 
                                                request.getInsuredAmount(), 
                                                riskType);
        
        PolicyRequestStatus newStatus = isValid ? PolicyRequestStatus.VALIDATED : PolicyRequestStatus.REJECTED;
        updateStatus(id, newStatus);
    }

    @Override
    @Transactional
    public void processFraudAnalysis(UUID id) {
        PolicyRequest request = findById(id);
        
        try {
            RiskAnalysis riskAnalysis = fraudAnalysisService.analyzeFraud(request);
            request.setRiskAnalysis(riskAnalysis);
            repository.save(request);
            validatePolicyRequest(id);
        } catch (Exception e) {
            log.error("Error analyzing fraud for policy request: {}", id, e);
            request.setStatus(PolicyRequestStatus.REJECTED);
            repository.save(request);
            eventPublisher.publish(
                RabbitMQConfig.POLICY_EVENTS_EXCHANGE,
                RabbitMQConfig.POLICY_REJECTED_KEY,
                new PolicyRejectedEvent(request)
            );
        }
    }

    @Override
    @Transactional
    public void processPayment(UUID id) {
        PolicyRequest request = findById(id);
        
        if (request.getStatus() != PolicyRequestStatus.VALIDATED) {
            throw new IllegalStateException("Cannot process payment for non-validated policy request");
        }
        
        boolean success = paymentService.processPayment(request);
        
        if (success) {
            request.setStatus(PolicyRequestStatus.PENDING);
            repository.save(request);
            eventPublisher.publish(
                RabbitMQConfig.POLICY_EVENTS_EXCHANGE,
                RabbitMQConfig.PAYMENT_PROCESSED_KEY,
                new PaymentProcessedEvent(request)
            );
        } else {
            request.setStatus(PolicyRequestStatus.REJECTED);
            repository.save(request);
            eventPublisher.publish(
                RabbitMQConfig.POLICY_EVENTS_EXCHANGE,
                RabbitMQConfig.PAYMENT_REJECTED_KEY,
                new PaymentRejectedEvent(request)
            );
        }
    }

    @Override
    @Transactional
    public void processSubscription(UUID id) {
        PolicyRequest request = findById(id);
        
        try {
            subscriptionService.processSubscription(request);
            request.setStatus(PolicyRequestStatus.APPROVED);
            repository.save(request);
            eventPublisher.publish(
                RabbitMQConfig.POLICY_EVENTS_EXCHANGE,
                RabbitMQConfig.POLICY_APPROVED_KEY,
                new SubscriptionApprovedEvent(request)
            );
        } catch (Exception e) {
            log.error("Error processing subscription for policy request: {}", id, e);
            request.setStatus(PolicyRequestStatus.REJECTED);
            repository.save(request);
            eventPublisher.publish(
                RabbitMQConfig.POLICY_EVENTS_EXCHANGE,
                RabbitMQConfig.POLICY_REJECTED_KEY,
                new PolicyRejectedEvent(request)
            );
        }
    }

    @Override
    @Transactional
    public void cancelPolicyRequest(UUID id) {
        PolicyRequest request = findById(id);
        
        if (request.getStatus() == PolicyRequestStatus.APPROVED) {
            throw new IllegalStateException("Cannot cancel an approved policy request");
        }
        
        request.setStatus(PolicyRequestStatus.CANCELLED);
        repository.save(request);
        
        eventPublisher.publish(
            RabbitMQConfig.POLICY_EVENTS_EXCHANGE,
            "policy.cancelled",
            new PolicyCancelledEvent(request)
        );
    }

    private boolean validateInsuranceAmount(InsuranceCategory category, BigDecimal amount, CustomerRiskType riskType) {
        return switch (riskType) {
            case REGULAR -> amount.compareTo(new BigDecimal("500000.00")) <= 0;
            case HIGH_RISK -> amount.compareTo(new BigDecimal("50000.00")) <= 0;
            case PREFERRED -> amount.compareTo(new BigDecimal("1000000.00")) <= 0;
            case NO_INFORMATION -> category == InsuranceCategory.LIFE ? 
                amount.compareTo(new BigDecimal("100000.00")) <= 0 :
                amount.compareTo(new BigDecimal("50000.00")) <= 0;
        };
    }
} 