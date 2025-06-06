package com.insurance.service.impl;

import com.insurance.domain.PolicyRequest;
import com.insurance.domain.RiskAnalysis;
import com.insurance.domain.RiskOccurrence;
import com.insurance.service.FraudAnalysisService;
import com.insurance.infrastructure.client.FraudAnalysisClient;
import com.insurance.infrastructure.client.dto.FraudAnalysisResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class FraudAnalysisServiceImpl implements FraudAnalysisService {

    private final FraudAnalysisClient fraudAnalysisClient;

    @Override
    @Transactional
    public RiskAnalysis analyzeFraud(PolicyRequest request) {
        validateRequest(request);

        log.info("Starting fraud analysis for policy request: {}", request.getId());

        FraudAnalysisResponse response = fraudAnalysisClient.analyzeFraud(
            request.getId(),
            request.getCustomerId()
        );

        validateResponse(response);

        RiskAnalysis riskAnalysis = new RiskAnalysis();
        riskAnalysis.setClassification(response.getClassification());
        riskAnalysis.setAnalyzedAt(response.getAnalyzedAt());
        riskAnalysis.setOccurrences(mapOccurrences(response.getOccurrences()));

        return riskAnalysis;
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
    }

    private void validateResponse(FraudAnalysisResponse response) {
        if (response == null) {
            throw new IllegalStateException("Fraud analysis response cannot be null");
        }
        if (response.getClassification() == null) {
            throw new IllegalStateException("Risk classification cannot be null");
        }
        if (response.getAnalyzedAt() == null) {
            throw new IllegalStateException("Analysis date cannot be null");
        }
        if (response.getAnalyzedAt().isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("Analysis date cannot be in the future");
        }
    }

    private List<RiskOccurrence> mapOccurrences(List<FraudAnalysisResponse.RiskOccurrenceResponse> occurrences) {
        if (occurrences == null) {
            return new ArrayList<>();
        }

        return occurrences.stream()
            .map(this::mapOccurrence)
            .toList();
    }

    private RiskOccurrence mapOccurrence(FraudAnalysisResponse.RiskOccurrenceResponse response) {
        if (response.getType() == null || response.getType().isEmpty()) {
            throw new IllegalArgumentException("Risk occurrence type cannot be empty");
        }
        if (response.getDescription() == null || response.getDescription().isEmpty()) {
            throw new IllegalArgumentException("Risk occurrence description cannot be empty");
        }
        if (response.getCreatedAt() == null) {
            throw new IllegalArgumentException("Risk occurrence creation date cannot be null");
        }
        if (response.getUpdatedAt() == null) {
            throw new IllegalArgumentException("Risk occurrence update date cannot be null");
        }

        RiskOccurrence occurrence = new RiskOccurrence();
        occurrence.setType(response.getType());
        occurrence.setDescription(response.getDescription());
        occurrence.setCreatedAt(response.getCreatedAt());
        occurrence.setUpdatedAt(response.getUpdatedAt());
        return occurrence;
    }
} 