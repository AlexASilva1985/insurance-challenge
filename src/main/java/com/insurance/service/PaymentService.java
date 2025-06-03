package com.insurance.service;

import com.insurance.domain.PolicyRequest;

public interface PaymentService {
    /**
     * Processa o pagamento de uma solicitação
     */
    void processPayment(PolicyRequest request);
} 