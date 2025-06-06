package com.insurance.event;

import com.insurance.domain.PolicyRequest;
import com.insurance.domain.enums.PolicyRequestStatus;

public class PolicyValidatedEvent extends PolicyRequestEvent {
    public PolicyValidatedEvent(PolicyRequest request) {
        super(request.getId(), request.getCustomerId(), request.getStatus());
    }
} 