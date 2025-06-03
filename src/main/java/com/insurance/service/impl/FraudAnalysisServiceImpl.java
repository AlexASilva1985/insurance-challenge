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

@Service
@RequiredArgsConstructor
@Slf4j
public class FraudAnalysisServiceImpl implements FraudAnalysisService {

    private final FraudAnalysisClient fraudAnalysisClient;

    @Override
    public RiskAnalysis analyzeFraud(PolicyRequest request) {
        log.info("Analyzing fraud for policy request: {}", request.getId());
        
        FraudAnalysisResponse response = fraudAnalysisClient.analyzeFraud(
            request.getId(), 
            request.getCustomerId()
        );

        RiskAnalysis riskAnalysis = new RiskAnalysis();
        riskAnalysis.setClassification(response.getClassification());
        riskAnalysis.setAnalyzedAt(response.getAnalyzedAt());
        riskAnalysis.setOccurrences(response.getOccurrences().stream()
            .map(this::mapToRiskOccurrence)
            .toList());

        return riskAnalysis;
    }

    private RiskOccurrence mapToRiskOccurrence(FraudAnalysisResponse.RiskOccurrenceResponse response) {
        RiskOccurrence occurrence = new RiskOccurrence();
        occurrence.setProductId(response.getId());
        occurrence.setType(response.getType());
        occurrence.setDescription(response.getDescription());
        occurrence.setCreatedAt(response.getCreatedAt());
        occurrence.setUpdatedAt(response.getUpdatedAt());
        return occurrence;
    }
} 