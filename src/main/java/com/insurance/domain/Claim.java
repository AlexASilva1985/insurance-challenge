package com.insurance.domain;

import com.insurance.domain.enums.ClaimStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "claims")
@Data
@EqualsAndHashCode(callSuper = true)
public class Claim extends BaseEntity {

    @Column(name = "claim_number", nullable = false, unique = true)
    private String claimNumber;

    @Column(name = "incident_date", nullable = false)
    private LocalDate incidentDate;

    @Column(nullable = false)
    private String description;

    @Column(name = "claim_amount", nullable = false)
    private BigDecimal claimAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ClaimStatus status = ClaimStatus.SUBMITTED;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "policy_id", nullable = false)
    private InsurancePolicy policy;

    @Column(name = "supporting_documents")
    private String supportingDocuments;

    @Column(name = "adjustor_notes")
    private String adjustorNotes;

    public void setClaimNumber(String claimNumber) {
        if (claimNumber == null) {
            throw new IllegalArgumentException("claimNumber cannot be null");
        }
        if (claimNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("empty");
        }
        if (!claimNumber.startsWith("CLM")) {
            throw new IllegalArgumentException("CLM");
        }
        if (claimNumber.length() < 11) {
            throw new IllegalArgumentException("length");
        }
        this.claimNumber = claimNumber;
    }

    public void setIncidentDate(LocalDate incidentDate) {
        if (incidentDate == null) {
            throw new IllegalArgumentException("incidentDate cannot be null");
        }
        if (incidentDate.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("future date");
        }
        if (policy != null && incidentDate.isBefore(policy.getStartDate())) {
            throw new IllegalArgumentException("policy start date");
        }
        this.incidentDate = incidentDate;
    }

    public void setDescription(String description) {
        if (description == null || description.trim().isEmpty()) {
            throw new IllegalArgumentException("description cannot be empty");
        }
        this.description = description;
    }

    public void setClaimAmount(BigDecimal claimAmount) {
        if (claimAmount == null) {
            throw new IllegalArgumentException("claimAmount cannot be null");
        }
        if (claimAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("negative");
        }
        if (claimAmount.compareTo(BigDecimal.ZERO) == 0) {
            throw new IllegalArgumentException("zero");
        }
        if (policy != null && claimAmount.compareTo(policy.getCoverageAmount()) > 0) {
            throw new IllegalArgumentException("coverage amount");
        }
        this.claimAmount = claimAmount;
    }

    public void setStatus(ClaimStatus newStatus) {
        if (newStatus == null) {
            throw new IllegalArgumentException("status cannot be null");
        }
        if (this.status != null && !isValidStatusTransition(this.status, newStatus)) {
            throw new IllegalStateException("Invalid status transition from " + this.status + " to " + newStatus);
        }
        this.status = newStatus;
    }

    private boolean isValidStatusTransition(ClaimStatus from, ClaimStatus to) {
        switch (from) {
            case SUBMITTED:
                return to == ClaimStatus.UNDER_REVIEW || to == ClaimStatus.REJECTED;
            case UNDER_REVIEW:
                return to == ClaimStatus.APPROVED || to == ClaimStatus.REJECTED;
            case APPROVED:
                return to == ClaimStatus.PAID;
            case PAID:
            case REJECTED:
                return false; // Estados finais
            default:
                return false;
        }
    }

    public void validate() {
        if (claimNumber == null || claimNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("claimNumber is required");
        }
        if (incidentDate == null) {
            throw new IllegalArgumentException("incidentDate is required");
        }
        if (description == null || description.trim().isEmpty()) {
            throw new IllegalArgumentException("description is required");
        }
        if (claimAmount == null) {
            throw new IllegalArgumentException("claimAmount is required");
        }
        if (policy == null) {
            throw new IllegalArgumentException("policy is required");
        }
    }

    @PrePersist
    public void onCreate() {
        if (status == null) {
            status = ClaimStatus.SUBMITTED;
        }
    }
} 