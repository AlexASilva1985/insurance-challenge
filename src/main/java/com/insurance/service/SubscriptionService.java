package com.insurance.service;

import com.insurance.domain.PolicyRequest;

public interface SubscriptionService {
    /**
     * Processa a subscrição de uma solicitação
     */
    void processSubscription(PolicyRequest request);
} 