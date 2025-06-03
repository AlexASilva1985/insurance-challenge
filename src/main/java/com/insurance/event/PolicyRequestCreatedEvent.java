package com.insurance.event;

import com.insurance.domain.PolicyRequest;
import com.insurance.domain.enums.PolicyRequestStatus;

public class PolicyRequestCreatedEvent extends PolicyRequestEvent {
    public PolicyRequestCreatedEvent(PolicyRequest request) {
        super(request.getId(), request.getCustomerId(), PolicyRequestStatus.RECEIVED);
    }
} 