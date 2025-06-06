package com.insurance.event;

import com.insurance.domain.PolicyRequest;
import com.insurance.domain.enums.PolicyRequestStatus;

public class PolicyStatusChangedEvent extends PolicyRequestEvent {
    public PolicyStatusChangedEvent(PolicyRequest request) {
        super(request.getId(), request.getCustomerId(), request.getStatus());
    }
} 