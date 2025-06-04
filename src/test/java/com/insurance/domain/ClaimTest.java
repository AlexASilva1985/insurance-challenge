package com.insurance.domain;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
}