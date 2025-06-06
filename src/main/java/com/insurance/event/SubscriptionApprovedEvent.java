package com.insurance.event;

import com.insurance.domain.PolicyRequest;
import com.insurance.domain.enums.PolicyRequestStatus;

public class SubscriptionApprovedEvent extends PolicyRequestEvent {
    public SubscriptionApprovedEvent(PolicyRequest request) {
        super(request.getId(), request.getCustomerId(), request.getStatus());
    }
} 