package com.insurance.event;

import com.insurance.domain.PolicyRequest;

public class FraudAnalysisRequestedEvent extends PolicyRequestEvent {
    public FraudAnalysisRequestedEvent(PolicyRequest request) {
        super(request.getId(), request.getCustomerId(), request.getStatus());
    }
} 