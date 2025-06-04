package com.insurance.infrastructure.client.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.insurance.domain.enums.CustomerRiskType;
import com.insurance.infrastructure.client.dto.FraudAnalysisResponse.RiskOccurrenceResponse;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class FraudAnalysisResponseTest {

    private FraudAnalysisResponse fraudAnalysisResponse;
    private RiskOccurrenceResponse riskOccurrenceResponse;
    private UUID orderId;
    private UUID customerId;
    private LocalDateTime analyzedAt;

    @BeforeEach
    void setUp() {
        orderId = UUID.randomUUID();
        customerId = UUID.randomUUID();
        analyzedAt = LocalDateTime.now();

        fraudAnalysisResponse = new FraudAnalysisResponse();
        fraudAnalysisResponse.setOrderId(orderId);
        fraudAnalysisResponse.setCustomerId(customerId);
        fraudAnalysisResponse.setAnalyzedAt(analyzedAt);
        fraudAnalysisResponse.setClassification(CustomerRiskType.REGULAR);

        riskOccurrenceResponse = new RiskOccurrenceResponse();
        riskOccurrenceResponse.setId(UUID.randomUUID());
        riskOccurrenceResponse.setProductId(12345L);
        riskOccurrenceResponse.setType("FRAUD");
        riskOccurrenceResponse.setDescription("Suspicious activity detected");
        riskOccurrenceResponse.setCreatedAt(LocalDateTime.now().minusDays(1));
        riskOccurrenceResponse.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    void testFraudAnalysisResponseGettersAndSetters() {
        assertEquals(orderId, fraudAnalysisResponse.getOrderId());
        assertEquals(customerId, fraudAnalysisResponse.getCustomerId());
        assertEquals(analyzedAt, fraudAnalysisResponse.getAnalyzedAt());
        assertEquals(CustomerRiskType.REGULAR, fraudAnalysisResponse.getClassification());
    }

    @Test
    void testFraudAnalysisResponseAllClassifications() {
        CustomerRiskType[] riskTypes = {
            CustomerRiskType.HIGH_RISK,
            CustomerRiskType.REGULAR,
            CustomerRiskType.PREFERRED,
            CustomerRiskType.HIGH_RISK
        };

        for (CustomerRiskType riskType : riskTypes) {
            fraudAnalysisResponse.setClassification(riskType);
            assertEquals(riskType, fraudAnalysisResponse.getClassification());
        }
    }

    @Test
    void testFraudAnalysisResponseWithOccurrences() {
        List<RiskOccurrenceResponse> occurrences = new ArrayList<>();
        occurrences.add(riskOccurrenceResponse);

        RiskOccurrenceResponse secondOccurrence = new RiskOccurrenceResponse();
        secondOccurrence.setId(UUID.randomUUID());
        secondOccurrence.setProductId(67890L);
        secondOccurrence.setType("IDENTITY_THEFT");
        secondOccurrence.setDescription("Identity verification failed");
        secondOccurrence.setCreatedAt(LocalDateTime.now().minusDays(2));
        secondOccurrence.setUpdatedAt(LocalDateTime.now());
        occurrences.add(secondOccurrence);

        fraudAnalysisResponse.setOccurrences(occurrences);

        assertEquals(2, fraudAnalysisResponse.getOccurrences().size());
        assertEquals("FRAUD", fraudAnalysisResponse.getOccurrences().get(0).getType());
        assertEquals("IDENTITY_THEFT", fraudAnalysisResponse.getOccurrences().get(1).getType());
    }

    @Test
    void testRiskOccurrenceResponseGettersAndSetters() {
        UUID occurrenceId = UUID.randomUUID();
        Long productId = 99999L;
        String type = "SUSPICIOUS_PATTERN";
        String description = "Unusual behavior pattern detected";
        LocalDateTime createdAt = LocalDateTime.now().minusHours(5);
        LocalDateTime updatedAt = LocalDateTime.now().minusHours(1);

        riskOccurrenceResponse.setId(occurrenceId);
        riskOccurrenceResponse.setProductId(productId);
        riskOccurrenceResponse.setType(type);
        riskOccurrenceResponse.setDescription(description);
        riskOccurrenceResponse.setCreatedAt(createdAt);
        riskOccurrenceResponse.setUpdatedAt(updatedAt);

        assertEquals(occurrenceId, riskOccurrenceResponse.getId());
        assertEquals(productId, riskOccurrenceResponse.getProductId());
        assertEquals(type, riskOccurrenceResponse.getType());
        assertEquals(description, riskOccurrenceResponse.getDescription());
        assertEquals(createdAt, riskOccurrenceResponse.getCreatedAt());
        assertEquals(updatedAt, riskOccurrenceResponse.getUpdatedAt());
    }

    @Test
    void testFraudAnalysisResponseEqualsAndHashCode() {
        FraudAnalysisResponse response1 = new FraudAnalysisResponse();
        FraudAnalysisResponse response2 = new FraudAnalysisResponse();

        response1.setOrderId(orderId);
        response1.setCustomerId(customerId);
        response1.setClassification(CustomerRiskType.REGULAR);

        response2.setOrderId(orderId);
        response2.setCustomerId(customerId);
        response2.setClassification(CustomerRiskType.REGULAR);

        assertEquals(response1, response2);
        assertEquals(response1.hashCode(), response2.hashCode());

        response2.setClassification(CustomerRiskType.HIGH_RISK);
        assertNotEquals(response1, response2);
    }

    @Test
    void testRiskOccurrenceResponseEqualsAndHashCode() {
        RiskOccurrenceResponse occurrence1 = new RiskOccurrenceResponse();
        RiskOccurrenceResponse occurrence2 = new RiskOccurrenceResponse();

        UUID occurrenceId = UUID.randomUUID();
        occurrence1.setId(occurrenceId);
        occurrence1.setType("FRAUD");

        occurrence2.setId(occurrenceId);
        occurrence2.setType("FRAUD");

        assertEquals(occurrence1, occurrence2);
        assertEquals(occurrence1.hashCode(), occurrence2.hashCode());

        occurrence2.setType("IDENTITY_THEFT");
        assertNotEquals(occurrence1, occurrence2);
    }

    @Test
    void testFraudAnalysisResponseToString() {
        String toString = fraudAnalysisResponse.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("FraudAnalysisResponse"));
    }

    @Test
    void testRiskOccurrenceResponseToString() {
        String toString = riskOccurrenceResponse.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("RiskOccurrenceResponse"));
    }

    @Test
    void testNullValues() {
        FraudAnalysisResponse response = new FraudAnalysisResponse();
        response.setOrderId(null);
        response.setCustomerId(null);
        response.setAnalyzedAt(null);
        response.setClassification(null);
        response.setOccurrences(null);

        assertEquals(null, response.getOrderId());
        assertEquals(null, response.getCustomerId());
        assertEquals(null, response.getAnalyzedAt());
        assertEquals(null, response.getClassification());
        assertEquals(null, response.getOccurrences());

        RiskOccurrenceResponse occurrence = new RiskOccurrenceResponse();
        occurrence.setId(null);
        occurrence.setProductId(null);
        occurrence.setType(null);
        occurrence.setDescription(null);
        occurrence.setCreatedAt(null);
        occurrence.setUpdatedAt(null);

        assertEquals(null, occurrence.getId());
        assertEquals(null, occurrence.getProductId());
        assertEquals(null, occurrence.getType());
        assertEquals(null, occurrence.getDescription());
        assertEquals(null, occurrence.getCreatedAt());
        assertEquals(null, occurrence.getUpdatedAt());
    }

    @Test
    void testEmptyOccurrencesList() {
        fraudAnalysisResponse.setOccurrences(new ArrayList<>());
        assertNotNull(fraudAnalysisResponse.getOccurrences());
        assertTrue(fraudAnalysisResponse.getOccurrences().isEmpty());
    }

    @Test
    void testRiskOccurrenceResponseTypes() {
        String[] riskTypes = {
            "FRAUD",
            "IDENTITY_THEFT", 
            "SUSPICIOUS_PATTERN",
            "BLACKLIST",
            "VELOCITY_CHECK",
            "DOCUMENT_FRAUD"
        };

        for (String type : riskTypes) {
            riskOccurrenceResponse.setType(type);
            assertEquals(type, riskOccurrenceResponse.getType());
        }
    }
} 