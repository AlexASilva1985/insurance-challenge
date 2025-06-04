package com.insurance.infrastructure.client;

import com.insurance.infrastructure.client.dto.FraudAnalysisResponse;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class FraudAnalysisClient {

    private final RestTemplate restTemplate;

    @Value("${fraud.api.url}")
    private String fraudApiUrl;

    public FraudAnalysisResponse analyzeFraud(UUID orderId, UUID customerId) {
        var request = new FraudAnalysisRequest(orderId, customerId);
        return restTemplate.postForObject(fraudApiUrl, request, FraudAnalysisResponse.class);
    }

    record FraudAnalysisRequest(UUID orderId, UUID customerId) {}
} 