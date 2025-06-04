package com.insurance.infrastructure.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.insurance.domain.enums.CustomerRiskType;
import com.insurance.infrastructure.client.dto.FraudAnalysisResponse;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@ExtendWith(MockitoExtension.class)
class FraudAnalysisClientTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private FraudAnalysisClient fraudAnalysisClient;

    private UUID orderId;
    private UUID customerId;
    private String fraudApiUrl;
    private FraudAnalysisResponse mockResponse;

    @BeforeEach
    void setUp() {
        orderId = UUID.randomUUID();
        customerId = UUID.randomUUID();
        fraudApiUrl = "http://fraud-api/analyze";
        
        ReflectionTestUtils.setField(fraudAnalysisClient, "fraudApiUrl", fraudApiUrl);
        
        mockResponse = new FraudAnalysisResponse();
        mockResponse.setOrderId(orderId);
        mockResponse.setCustomerId(customerId);
        mockResponse.setAnalyzedAt(LocalDateTime.now());
        mockResponse.setClassification(CustomerRiskType.REGULAR);
        mockResponse.setOccurrences(new ArrayList<>());
    }

    @Test
    void testAnalyzeFraudSuccessful() {
        when(restTemplate.postForObject(
            eq(fraudApiUrl),
            any(FraudAnalysisClient.FraudAnalysisRequest.class),
            eq(FraudAnalysisResponse.class)
        )).thenReturn(mockResponse);

        FraudAnalysisResponse response = fraudAnalysisClient.analyzeFraud(orderId, customerId);

        assertNotNull(response);
        assertEquals(orderId, response.getOrderId());
        assertEquals(customerId, response.getCustomerId());
        assertEquals(CustomerRiskType.REGULAR, response.getClassification());
        
        verify(restTemplate).postForObject(
            eq(fraudApiUrl),
            eq(new FraudAnalysisClient.FraudAnalysisRequest(orderId, customerId)),
            eq(FraudAnalysisResponse.class)
        );
    }

    @Test
    void testAnalyzeFraudWithHighRisk() {
        mockResponse.setClassification(CustomerRiskType.HIGH_RISK);
        
        when(restTemplate.postForObject(
            eq(fraudApiUrl),
            any(FraudAnalysisClient.FraudAnalysisRequest.class),
            eq(FraudAnalysisResponse.class)
        )).thenReturn(mockResponse);

        FraudAnalysisResponse response = fraudAnalysisClient.analyzeFraud(orderId, customerId);

        assertNotNull(response);
        assertEquals(CustomerRiskType.HIGH_RISK, response.getClassification());
    }

    @Test
    void testAnalyzeFraudApiError() {
        when(restTemplate.postForObject(
            eq(fraudApiUrl),
            any(FraudAnalysisClient.FraudAnalysisRequest.class),
            eq(FraudAnalysisResponse.class)
        )).thenThrow(new RestClientException("API Error"));

        assertThrows(RestClientException.class, () -> 
            fraudAnalysisClient.analyzeFraud(orderId, customerId)
        );
    }

    @Test
    void testAnalyzeFraudRequestCreation() {
        var request = new FraudAnalysisClient.FraudAnalysisRequest(orderId, customerId);
        
        assertEquals(orderId, request.orderId());
        assertEquals(customerId, request.customerId());
    }
} 