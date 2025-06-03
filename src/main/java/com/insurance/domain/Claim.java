package com.insurance.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Getter
@Setter
public class Claim extends BaseEntity {
    @NotNull
    private String claimNumber;
    
    @NotNull
    private LocalDate incidentDate;
    
    @NotNull
    private String description;
    
    @NotNull
    private BigDecimal claimAmount;
    
    @Enumerated(EnumType.STRING)
    private ClaimStatus status;
    
    @ManyToOne
    @JoinColumn(name = "policy_id")
    private InsurancePolicy policy;
    
    private String supportingDocuments;
    
    private String adjustorNotes;
}

@Getter
enum ClaimStatus {
    SUBMITTED,
    UNDER_REVIEW,
    APPROVED,
    REJECTED,
    PAID
} 