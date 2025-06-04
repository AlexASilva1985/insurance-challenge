package com.insurance.infrastructure.client.dto;

import com.insurance.domain.enums.CustomerRiskType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.Data;

@Data
public class FraudAnalysisResponse {
    private UUID orderId;
    private UUID customerId;
    private LocalDateTime analyzedAt;
    private CustomerRiskType classification;
    private List<RiskOccurrenceResponse> occurrences;

    @Data
    public static class RiskOccurrenceResponse {
        private UUID id;
        private Long productId;
        private String type;
        private String description;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }
} 