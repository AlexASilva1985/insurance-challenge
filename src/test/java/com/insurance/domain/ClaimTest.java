package com.insurance.domain;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.insurance.domain.enums.ClaimStatus;
import com.insurance.domain.enums.InsuranceType;
import com.insurance.domain.enums.PolicyStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ClaimTest {

    private Claim claim;
    private InsurancePolicy policy;
    private LocalDate incidentDate;
    private String claimNumber;

    @BeforeEach
    void setUp() {
        // Configurando a apólice
        policy = new InsurancePolicy();
        policy.setPolicyNumber("POL-2024-001");
        policy.setStartDate(LocalDate.now().minusMonths(1));
        policy.setEndDate(LocalDate.now().plusMonths(11));
        policy.setPremium(new BigDecimal("1000.00"));
        policy.setCoverageAmount(new BigDecimal("50000.00"));
        policy.setStatus(PolicyStatus.ACTIVE);
        policy.setType(InsuranceType.AUTO);

        // Configurando a data do incidente
        incidentDate = LocalDate.now().minusDays(5);
        
        // Configurando o número do sinistro
        claimNumber = "CLM" + UUID.randomUUID().toString().substring(0, 8);

        // Configurando o sinistro
        claim = new Claim();
        claim.setClaimNumber(claimNumber);
        claim.setIncidentDate(incidentDate);
        claim.setDescription("Car accident on highway");
        claim.setClaimAmount(new BigDecimal("5000.00"));
        claim.setPolicy(policy);
        claim.setSupportingDocuments("police_report.pdf, photos.zip");
        claim.setAdjustorNotes("Initial assessment completed");
    }

    @Test
    void testCreateClaimWithCorrectData() {
        assertNotNull(claim);
        assertEquals(claimNumber, claim.getClaimNumber());
        assertEquals(incidentDate, claim.getIncidentDate());
        assertEquals("Car accident on highway", claim.getDescription());
        assertEquals(new BigDecimal("5000.00"), claim.getClaimAmount());
        assertEquals(ClaimStatus.SUBMITTED, claim.getStatus());
        assertEquals(policy, claim.getPolicy());
        assertTrue(claim.getSupportingDocuments().contains("police_report.pdf"));
        assertEquals("Initial assessment completed", claim.getAdjustorNotes());
    }

    @Test
    void testValidateRequiredFields() {
        Claim invalidClaim = new Claim();
        
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            invalidClaim.setClaimNumber(null);
        });
        assertTrue(exception.getMessage().contains("claimNumber"));

        exception = assertThrows(IllegalArgumentException.class, () -> {
            invalidClaim.setIncidentDate(null);
        });
        assertTrue(exception.getMessage().contains("incidentDate"));

        exception = assertThrows(IllegalArgumentException.class, () -> {
            invalidClaim.setDescription(null);
        });
        assertTrue(exception.getMessage().contains("description"));

        exception = assertThrows(IllegalArgumentException.class, () -> {
            invalidClaim.setClaimAmount(null);
        });
        assertTrue(exception.getMessage().contains("claimAmount"));
    }

    @Test
    void testValidateIncidentDate() {
        // Data futura não deve ser permitida
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            claim.setIncidentDate(LocalDate.now().plusDays(1));
        });
        assertTrue(exception.getMessage().contains("future date"));

        // Data anterior ao início da apólice não deve ser permitida
        exception = assertThrows(IllegalArgumentException.class, () -> {
            claim.setIncidentDate(policy.getStartDate().minusDays(1));
        });
        assertTrue(exception.getMessage().contains("policy start date"));

        // Data válida deve ser aceita
        assertDoesNotThrow(() -> {
            claim.setIncidentDate(LocalDate.now().minusDays(1));
        });
    }

    @Test
    void testValidateClaimAmount() {
        // Valor negativo não deve ser permitido
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            claim.setClaimAmount(new BigDecimal("-1.00"));
        });
        assertTrue(exception.getMessage().contains("negative"));

        // Valor zero não deve ser permitido
        exception = assertThrows(IllegalArgumentException.class, () -> {
            claim.setClaimAmount(BigDecimal.ZERO);
        });
        assertTrue(exception.getMessage().contains("zero"));

        // Valor maior que a cobertura não deve ser permitido
        exception = assertThrows(IllegalArgumentException.class, () -> {
            claim.setClaimAmount(policy.getCoverageAmount().add(BigDecimal.ONE));
        });
        assertTrue(exception.getMessage().contains("coverage amount"));

        // Valor válido deve ser aceito
        assertDoesNotThrow(() -> {
            claim.setClaimAmount(new BigDecimal("1000.00"));
        });
    }

    @Test
    void testHandleStatusTransitions() {
        // Iniciando com SUBMITTED
        assertEquals(ClaimStatus.SUBMITTED, claim.getStatus());

        // Transição válida para UNDER_REVIEW
        assertDoesNotThrow(() -> {
            claim.setStatus(ClaimStatus.UNDER_REVIEW);
        });
        assertEquals(ClaimStatus.UNDER_REVIEW, claim.getStatus());

        // Transição válida para APPROVED
        assertDoesNotThrow(() -> {
            claim.setStatus(ClaimStatus.APPROVED);
        });
        assertEquals(ClaimStatus.APPROVED, claim.getStatus());

        // Transição válida para PAID
        assertDoesNotThrow(() -> {
            claim.setStatus(ClaimStatus.PAID);
        });
        assertEquals(ClaimStatus.PAID, claim.getStatus());

        // Não deve permitir voltar para SUBMITTED após PAID
        Exception exception = assertThrows(IllegalStateException.class, () -> {
            claim.setStatus(ClaimStatus.SUBMITTED);
        });
        assertTrue(exception.getMessage().contains("Invalid status transition"));
    }

    @Test
    void testValidateClaimNumber() {
        // Número do sinistro não pode ser vazio
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            claim.setClaimNumber("");
        });
        assertTrue(exception.getMessage().contains("empty"));

        // Número do sinistro não pode ser muito curto
        exception = assertThrows(IllegalArgumentException.class, () -> {
            claim.setClaimNumber("CLM");
        });
        assertTrue(exception.getMessage().contains("length"));

        // Número do sinistro deve começar com CLM
        exception = assertThrows(IllegalArgumentException.class, () -> {
            claim.setClaimNumber("ABC12345678");
        });
        assertTrue(exception.getMessage().contains("CLM"));

        // Número do sinistro válido deve ser aceito
        assertDoesNotThrow(() -> {
            claim.setClaimNumber("CLM12345678");
        });
    }

    @Test
    void testClaimNumberValidation() {
        // Test null claim number
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            claim.setClaimNumber(null);
        });
        assertEquals("claimNumber cannot be null", exception.getMessage());

        // Test empty claim number
        exception = assertThrows(IllegalArgumentException.class, () -> {
            claim.setClaimNumber("");
        });
        assertTrue(exception.getMessage().contains("empty"));

        // Test whitespace only claim number
        exception = assertThrows(IllegalArgumentException.class, () -> {
            claim.setClaimNumber("   ");
        });
        assertTrue(exception.getMessage().contains("empty"));

        // Test claim number without CLM prefix
        exception = assertThrows(IllegalArgumentException.class, () -> {
            claim.setClaimNumber("ABC-2023-001");
        });
        assertTrue(exception.getMessage().contains("CLM"));

        // Test claim number too short
        exception = assertThrows(IllegalArgumentException.class, () -> {
            claim.setClaimNumber("CLM-123");
        });
        assertTrue(exception.getMessage().contains("length"));

        // Test valid claim number
        claim.setClaimNumber("CLM-2023-001");
        assertEquals("CLM-2023-001", claim.getClaimNumber());
    }

    @Test
    void testIncidentDateValidation() {
        // Test null incident date
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            claim.setIncidentDate(null);
        });
        assertEquals("incidentDate cannot be null", exception.getMessage());

        // Test future incident date
        exception = assertThrows(IllegalArgumentException.class, () -> {
            claim.setIncidentDate(LocalDate.now().plusDays(1));
        });
        assertTrue(exception.getMessage().contains("future date"));

        // Test past incident date (valid)
        LocalDate pastDate = LocalDate.now().minusDays(10);
        claim.setIncidentDate(pastDate);
        assertEquals(pastDate, claim.getIncidentDate());

        // Test today's date (valid)
        LocalDate today = LocalDate.now();
        claim.setIncidentDate(today);
        assertEquals(today, claim.getIncidentDate());
    }

    @Test
    void testIncidentDateWithPolicyValidation() {
        // Create a policy with specific start date
        InsurancePolicy policy = new InsurancePolicy();
        policy.setStartDate(LocalDate.now().minusDays(30));
        policy.setCoverageAmount(new BigDecimal("50000.00"));
        claim.setPolicy(policy);

        // Test incident date before policy start date
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            claim.setIncidentDate(LocalDate.now().minusDays(40));
        });
        assertTrue(exception.getMessage().contains("policy start date"));

        // Test valid incident date after policy start
        LocalDate validDate = LocalDate.now().minusDays(10);
        claim.setIncidentDate(validDate);
        assertEquals(validDate, claim.getIncidentDate());
    }

    @Test
    void testDescriptionValidation() {
        // Test null description
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            claim.setDescription(null);
        });
        assertEquals("description cannot be empty", exception.getMessage());

        // Test empty description
        exception = assertThrows(IllegalArgumentException.class, () -> {
            claim.setDescription("");
        });
        assertEquals("description cannot be empty", exception.getMessage());

        // Test whitespace only description
        exception = assertThrows(IllegalArgumentException.class, () -> {
            claim.setDescription("   ");
        });
        assertEquals("description cannot be empty", exception.getMessage());

        // Test valid description
        String validDescription = "Car accident on highway";
        claim.setDescription(validDescription);
        assertEquals(validDescription, claim.getDescription());
    }

    @Test
    void testClaimAmountValidation() {
        // Test null claim amount
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            claim.setClaimAmount(null);
        });
        assertEquals("claimAmount cannot be null", exception.getMessage());

        // Test negative claim amount
        exception = assertThrows(IllegalArgumentException.class, () -> {
            claim.setClaimAmount(new BigDecimal("-100.00"));
        });
        assertTrue(exception.getMessage().contains("negative"));

        // Test zero claim amount
        exception = assertThrows(IllegalArgumentException.class, () -> {
            claim.setClaimAmount(BigDecimal.ZERO);
        });
        assertTrue(exception.getMessage().contains("zero"));

        // Test valid claim amount
        BigDecimal validAmount = new BigDecimal("5000.00");
        claim.setClaimAmount(validAmount);
        assertEquals(validAmount, claim.getClaimAmount());
    }

    @Test
    void testClaimAmountWithPolicyValidation() {
        // Create a policy with specific coverage amount
        InsurancePolicy policy = new InsurancePolicy();
        policy.setCoverageAmount(new BigDecimal("10000.00"));
        policy.setStartDate(LocalDate.now().minusDays(30));
        claim.setPolicy(policy);

        // Test claim amount exceeding coverage
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            claim.setClaimAmount(new BigDecimal("15000.00"));
        });
        assertTrue(exception.getMessage().contains("coverage amount"));

        // Test valid claim amount within coverage
        BigDecimal validAmount = new BigDecimal("8000.00");
        claim.setClaimAmount(validAmount);
        assertEquals(validAmount, claim.getClaimAmount());

        // Test claim amount equal to coverage
        BigDecimal maxAmount = new BigDecimal("10000.00");
        claim.setClaimAmount(maxAmount);
        assertEquals(maxAmount, claim.getClaimAmount());
    }

    @Test
    void testStatusTransitions() {
        // Test valid transitions from SUBMITTED
        claim.setStatus(ClaimStatus.UNDER_REVIEW);
        assertEquals(ClaimStatus.UNDER_REVIEW, claim.getStatus());

        // Create new claim to test rejection from SUBMITTED
        Claim rejectedClaim = new Claim();
        rejectedClaim.setClaimNumber("CLM-REJECTED");
        rejectedClaim.setIncidentDate(LocalDate.now().minusDays(5));
        rejectedClaim.setDescription("Test rejected claim");
        rejectedClaim.setClaimAmount(new BigDecimal("1000.00"));
        
        InsurancePolicy policy = new InsurancePolicy();
        policy.setStartDate(LocalDate.now().minusDays(30));
        policy.setCoverageAmount(new BigDecimal("50000.00"));
        rejectedClaim.setPolicy(policy);
        
        rejectedClaim.setStatus(ClaimStatus.REJECTED);
        assertEquals(ClaimStatus.REJECTED, rejectedClaim.getStatus());

        // Test valid transitions from UNDER_REVIEW using original claim
        claim.setStatus(ClaimStatus.APPROVED);
        assertEquals(ClaimStatus.APPROVED, claim.getStatus());

        // Test transition from APPROVED to PAID
        claim.setStatus(ClaimStatus.PAID);
        assertEquals(ClaimStatus.PAID, claim.getStatus());
    }

    @Test
    void testInvalidStatusTransitions() {
        // Test invalid transition from SUBMITTED to APPROVED (must go through UNDER_REVIEW)
        Exception exception = assertThrows(IllegalStateException.class, () -> {
            claim.setStatus(ClaimStatus.APPROVED);
        });
        assertTrue(exception.getMessage().contains("Invalid status transition"));

        // Test invalid transition from APPROVED back to UNDER_REVIEW
        claim.setStatus(ClaimStatus.UNDER_REVIEW);
        claim.setStatus(ClaimStatus.APPROVED);
        exception = assertThrows(IllegalStateException.class, () -> {
            claim.setStatus(ClaimStatus.UNDER_REVIEW);
        });
        assertTrue(exception.getMessage().contains("Invalid status transition"));

        // Test invalid transition from PAID (final state)
        claim.setStatus(ClaimStatus.PAID);
        exception = assertThrows(IllegalStateException.class, () -> {
            claim.setStatus(ClaimStatus.UNDER_REVIEW);
        });
        assertTrue(exception.getMessage().contains("Invalid status transition"));

        // Create new claim to test invalid transitions from REJECTED
        Claim rejectedClaim = new Claim();
        rejectedClaim.setClaimNumber("CLM-REJECTED-TEST");
        rejectedClaim.setIncidentDate(LocalDate.now().minusDays(5));
        rejectedClaim.setDescription("Test rejected claim transitions");
        rejectedClaim.setClaimAmount(new BigDecimal("1000.00"));
        
        InsurancePolicy policy = new InsurancePolicy();
        policy.setStartDate(LocalDate.now().minusDays(30));
        policy.setCoverageAmount(new BigDecimal("50000.00"));
        rejectedClaim.setPolicy(policy);
        
        rejectedClaim.setStatus(ClaimStatus.REJECTED);
        exception = assertThrows(IllegalStateException.class, () -> {
            rejectedClaim.setStatus(ClaimStatus.APPROVED);
        });
        assertTrue(exception.getMessage().contains("Invalid status transition"));
    }

    @Test
    void testSetNullStatus() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            claim.setStatus(null);
        });
        assertEquals("status cannot be null", exception.getMessage());
    }

    @Test
    void testValidateWithMissingFields() {
        Claim emptyClaim = new Claim();

        // Test validation with missing claimNumber
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            emptyClaim.validate();
        });
        assertEquals("claimNumber is required", exception.getMessage());

        // Add claimNumber and test missing incidentDate
        emptyClaim.setClaimNumber("CLM-2023-002");
        exception = assertThrows(IllegalArgumentException.class, () -> {
            emptyClaim.validate();
        });
        assertEquals("incidentDate is required", exception.getMessage());

        // Add incidentDate and test missing description
        emptyClaim.setIncidentDate(LocalDate.now().minusDays(5));
        exception = assertThrows(IllegalArgumentException.class, () -> {
            emptyClaim.validate();
        });
        assertEquals("description is required", exception.getMessage());

        // Add description and test missing claimAmount
        emptyClaim.setDescription("Test incident");
        exception = assertThrows(IllegalArgumentException.class, () -> {
            emptyClaim.validate();
        });
        assertEquals("claimAmount is required", exception.getMessage());

        // Add claimAmount and test missing policy
        emptyClaim.setClaimAmount(new BigDecimal("1000.00"));
        exception = assertThrows(IllegalArgumentException.class, () -> {
            emptyClaim.validate();
        });
        assertEquals("policy is required", exception.getMessage());
    }

    @Test
    void testValidateWithAllRequiredFields() {
        Claim validClaim = new Claim();
        validClaim.setClaimNumber("CLM-2023-003");
        validClaim.setIncidentDate(LocalDate.now().minusDays(5));
        validClaim.setDescription("Valid claim description");
        validClaim.setClaimAmount(new BigDecimal("2000.00"));
        
        InsurancePolicy policy = new InsurancePolicy();
        policy.setStartDate(LocalDate.now().minusDays(30));
        policy.setCoverageAmount(new BigDecimal("50000.00"));
        validClaim.setPolicy(policy);

        // Should not throw any exception
        assertDoesNotThrow(() -> validClaim.validate());
    }

    @Test
    void testOnCreateSetsDefaultStatus() {
        Claim newClaim = new Claim();
        newClaim.onCreate();
        assertEquals(ClaimStatus.SUBMITTED, newClaim.getStatus());
    }

    @Test
    void testOnCreateDoesNotOverrideExistingStatus() {
        Claim newClaim = new Claim();
        newClaim.setStatus(ClaimStatus.UNDER_REVIEW);
        newClaim.onCreate();
        assertEquals(ClaimStatus.UNDER_REVIEW, newClaim.getStatus());
    }

    @Test
    void testSupportingDocuments() {
        String documents = "photo1.jpg, photo2.jpg, police_report.pdf";
        claim.setSupportingDocuments(documents);
        assertEquals(documents, claim.getSupportingDocuments());

        // Test with null (should be allowed)
        claim.setSupportingDocuments(null);
        assertNull(claim.getSupportingDocuments());

        // Test with empty string (should be allowed)
        claim.setSupportingDocuments("");
        assertEquals("", claim.getSupportingDocuments());
    }

    @Test
    void testAdjustorNotes() {
        String notes = "Claim reviewed and approved based on provided evidence";
        claim.setAdjustorNotes(notes);
        assertEquals(notes, claim.getAdjustorNotes());

        // Test with null (should be allowed)
        claim.setAdjustorNotes(null);
        assertNull(claim.getAdjustorNotes());

        // Test with empty string (should be allowed)
        claim.setAdjustorNotes("");
        assertEquals("", claim.getAdjustorNotes());
    }

    @Test
    void testClaimWithCompleteWorkflow() {
        // Create a complete claim workflow
        Claim workflowClaim = new Claim();
        
        // Set basic information
        workflowClaim.setClaimNumber("CLM-2023-WORKFLOW");
        workflowClaim.setIncidentDate(LocalDate.now().minusDays(7));
        workflowClaim.setDescription("Complete workflow test claim");
        workflowClaim.setClaimAmount(new BigDecimal("3500.00"));
        
        // Create and set policy
        InsurancePolicy policy = new InsurancePolicy();
        policy.setStartDate(LocalDate.now().minusDays(30));
        policy.setCoverageAmount(new BigDecimal("50000.00"));
        workflowClaim.setPolicy(policy);
        
        // Test initial state
        assertEquals(ClaimStatus.SUBMITTED, workflowClaim.getStatus());
        
        // Move to under review
        workflowClaim.setStatus(ClaimStatus.UNDER_REVIEW);
        workflowClaim.setAdjustorNotes("Claim under review - evidence being analyzed");
        
        // Approve the claim
        workflowClaim.setStatus(ClaimStatus.APPROVED);
        workflowClaim.setAdjustorNotes("Claim approved - payment authorized");
        
        // Mark as paid
        workflowClaim.setStatus(ClaimStatus.PAID);
        workflowClaim.setAdjustorNotes("Payment processed successfully");
        
        // Verify final state
        assertEquals(ClaimStatus.PAID, workflowClaim.getStatus());
        assertEquals("Payment processed successfully", workflowClaim.getAdjustorNotes());
        
        // Validate the complete claim
        assertDoesNotThrow(() -> workflowClaim.validate());
    }

    @Test
    void testInheritanceFromBaseEntity() {
        assertTrue(claim instanceof BaseEntity);
        
        // Test inherited methods
        claim.setCreatedBy("claims_processor");
        claim.setUpdatedBy("adjustor");
        
        assertEquals("claims_processor", claim.getCreatedBy());
        assertEquals("adjustor", claim.getUpdatedBy());
    }
}