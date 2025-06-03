package com.insurance.event;

import com.insurance.domain.enums.PolicyRequestStatus;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@RequiredArgsConstructor
public abstract class PolicyRequestEvent {
    private final UUID policyRequestId;
    private final UUID customerId;
    private final PolicyRequestStatus status;
    private final LocalDateTime timestamp = LocalDateTime.now();
    private final String eventType = this.getClass().getSimpleName();
} 