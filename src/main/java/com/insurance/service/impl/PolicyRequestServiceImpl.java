package com.insurance.service.impl;

import com.insurance.domain.PolicyRequest;
import com.insurance.domain.RiskAnalysis;
import com.insurance.domain.enums.CustomerRiskType;
import com.insurance.domain.enums.InsuranceCategory;
import com.insurance.domain.enums.PolicyRequestStatus;
import com.insurance.event.PolicyRequestCreatedEvent;
import com.insurance.event.PolicyRequestEvent;
import com.insurance.repository.PolicyRequestRepository;
import com.insurance.service.FraudAnalysisService;
import com.insurance.service.PaymentService;
import com.insurance.service.PolicyRequestService;
import com.insurance.service.SubscriptionService;
import com.insurance.infrastructure.messaging.config.RabbitMQConfig;
import com.insurance.infrastructure.messaging.service.EventPublisher;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

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
        
        request.addToHistory(newStatus);
        request = repository.save(request);
        
        String routingKey = switch (newStatus) {
            case VALIDATED -> RabbitMQConfig.POLICY_VALIDATED_KEY;
            case REJECTED -> RabbitMQConfig.POLICY_REJECTED_KEY;
            case APPROVED -> RabbitMQConfig.POLICY_APPROVED_KEY;
            case CANCELLED -> "policy.cancelled";
            default -> "policy.status.changed";
        };
        
        eventPublisher.publish(
            RabbitMQConfig.POLICY_EVENTS_EXCHANGE,
            routingKey,
            new PolicyRequestEvent(request.getId(), request.getCustomerId(), newStatus) {}
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
        log.info("Processing fraud analysis for policy request: {}", id);
        PolicyRequest request = findById(id);
        
        try {
            RiskAnalysis riskAnalysis = fraudAnalysisService.analyzeFraud(request);
            request.setRiskAnalysis(riskAnalysis);
            repository.save(request);
            
            validatePolicyRequest(id);
            
        } catch (Exception e) {
            log.error("Error processing fraud analysis for policy request: {}", id, e);
            updateStatus(id, PolicyRequestStatus.REJECTED);
        }
    }

    @Override
    @Transactional
    public void processPayment(UUID id) {
        log.info("Processing payment for policy request: {}", id);
        PolicyRequest request = findById(id);
        
        try {
            paymentService.processPayment(request);
            updateStatus(id, PolicyRequestStatus.PENDING);
        } catch (Exception e) {
            log.error("Error processing payment for policy request: {}", id, e);
            updateStatus(id, PolicyRequestStatus.REJECTED);
        }
    }

    @Override
    @Transactional
    public void processSubscription(UUID id) {
        log.info("Processing subscription for policy request: {}", id);
        PolicyRequest request = findById(id);
        
        try {
            subscriptionService.processSubscription(request);
            updateStatus(id, PolicyRequestStatus.APPROVED);
        } catch (Exception e) {
            log.error("Error processing subscription for policy request: {}", id, e);
            updateStatus(id, PolicyRequestStatus.REJECTED);
        }
    }

    @Override
    @Transactional
    public void cancelPolicyRequest(UUID id) {
        PolicyRequest request = findById(id);
        
        if (request.getStatus() == PolicyRequestStatus.APPROVED) {
            throw new IllegalStateException("Cannot cancel an approved policy request");
        }
        
        updateStatus(id, PolicyRequestStatus.CANCELLED);
    }

    private boolean validateInsuranceAmount(InsuranceCategory category, 
                                          BigDecimal amount, 
                                          CustomerRiskType riskType) {
        return switch (riskType) {
            case REGULAR -> validateRegularCustomer(category, amount);
            case HIGH_RISK -> validateHighRiskCustomer(category, amount);
            case PREFERRED -> validatePreferredCustomer(category, amount);
            case NO_INFORMATION -> validateNoInformationCustomer(category, amount);
        };
    }

    private boolean validateRegularCustomer(InsuranceCategory category, BigDecimal amount) {
        return switch (category) {
            case LIFE, RESIDENTIAL -> amount.compareTo(new BigDecimal("500000.00")) <= 0;
            case AUTO -> amount.compareTo(new BigDecimal("350000.00")) <= 0;
            default -> amount.compareTo(new BigDecimal("255000.00")) <= 0;
        };
    }

    private boolean validateHighRiskCustomer(InsuranceCategory category, BigDecimal amount) {
        return switch (category) {
            case AUTO -> amount.compareTo(new BigDecimal("250000.00")) <= 0;
            case RESIDENTIAL -> amount.compareTo(new BigDecimal("150000.00")) <= 0;
            default -> amount.compareTo(new BigDecimal("125000.00")) <= 0;
        };
    }

    private boolean validatePreferredCustomer(InsuranceCategory category, BigDecimal amount) {
        return switch (category) {
            case LIFE -> amount.compareTo(new BigDecimal("800000.00")) <= 0;
            case AUTO, RESIDENTIAL -> amount.compareTo(new BigDecimal("450000.00")) <= 0;
            default -> amount.compareTo(new BigDecimal("375000.00")) <= 0;
        };
    }

    private boolean validateNoInformationCustomer(InsuranceCategory category, BigDecimal amount) {
        return switch (category) {
            case LIFE, RESIDENTIAL -> amount.compareTo(new BigDecimal("200000.00")) <= 0;
            case AUTO -> amount.compareTo(new BigDecimal("75000.00")) <= 0;
            default -> amount.compareTo(new BigDecimal("55000.00")) <= 0;
        };
    }
} 