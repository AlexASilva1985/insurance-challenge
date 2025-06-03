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
public class InsurancePolicy extends BaseEntity {
    @NotNull
    private String policyNumber;
    
    @NotNull
    private LocalDate startDate;
    
    @NotNull
    private LocalDate endDate;
    
    @NotNull
    private BigDecimal premium;
    
    @NotNull
    private BigDecimal coverageAmount;
    
    @Enumerated(EnumType.STRING)
    private PolicyStatus status;
    
    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;
    
    @Enumerated(EnumType.STRING)
    private InsuranceType type;
}

@Getter
enum PolicyStatus {
    ACTIVE,
    PENDING,
    CANCELLED,
    EXPIRED
}

@Getter
enum InsuranceType {
    AUTO,
    LIFE,
    HOME,
    HEALTH,
    BUSINESS
} 