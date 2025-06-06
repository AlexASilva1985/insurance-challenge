package com.insurance.service;

import com.insurance.domain.PolicyRequest;

public interface PaymentService {
    /**
     * Processa o pagamento de uma solicitação
     * @return true se o pagamento foi processado com sucesso, false caso contrário
     */
    boolean processPayment(PolicyRequest request);
} 