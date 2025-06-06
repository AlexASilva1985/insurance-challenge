package com.insurance.event;

import com.insurance.domain.PolicyRequest;
import com.insurance.domain.enums.PolicyRequestStatus;

public class PolicyCancelledEvent extends PolicyRequestEvent {
    public PolicyCancelledEvent(PolicyRequest request) {
        super(request.getId(), request.getCustomerId(), request.getStatus());
    }
} 