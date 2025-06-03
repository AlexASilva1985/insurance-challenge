package com.insurance.domain.enums;

public enum PolicyRequestStatus {
    RECEIVED,      // Solicitação recebida
    VALIDATED,     // Validada após análise de risco
    PENDING,       // Aguardando pagamento
    APPROVED,      // Aprovada e apólice emitida
    REJECTED,      // Rejeitada
    CANCELLED      // Cancelada
} 