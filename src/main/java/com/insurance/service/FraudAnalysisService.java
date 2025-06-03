package com.insurance.service;

import com.insurance.domain.PolicyRequest;
import com.insurance.domain.RiskAnalysis;

public interface FraudAnalysisService {
    /**
     * Analisa o risco de fraude para uma solicitação
     */
    RiskAnalysis analyzeFraud(PolicyRequest request);
} 