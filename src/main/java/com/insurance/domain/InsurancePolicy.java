package com.insurance.domain;

import com.insurance.domain.enums.InsuranceType;
import com.insurance.domain.enums.PolicyStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "insurance_policies")
@Data
@EqualsAndHashCode(callSuper = true)
public class InsurancePolicy extends BaseEntity {

    @Column(name = "policy_number", nullable = false, unique = true)
    private String policyNumber;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(nullable = false)
    private BigDecimal premium;

    @Column(name = "coverage_amount", nullable = false)
    private BigDecimal coverageAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PolicyStatus status;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InsuranceType type;

    public void setPolicyNumber(String policyNumber) {
        if (policyNumber == null || policyNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("policyNumber cannot be empty");
        }
        if (!policyNumber.startsWith("POL-")) {
            throw new IllegalArgumentException("policyNumber must start with POL-");
        }
        this.policyNumber = policyNumber;
    }

    public void setStartDate(LocalDate startDate) {
        if (startDate == null) {
            throw new IllegalArgumentException("startDate cannot be null");
        }
        if (endDate != null && startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("startDate cannot be after endDate");
        }
        this.startDate = startDate;
    }

    public void setEndDate(LocalDate endDate) {
        if (endDate == null) {
            throw new IllegalArgumentException("endDate cannot be null");
        }
        if (startDate != null && endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("endDate cannot be before startDate");
        }
        this.endDate = endDate;
    }

    public void setPremium(BigDecimal premium) {
        if (premium == null) {
            throw new IllegalArgumentException("premium cannot be null");
        }
        if (premium.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("premium must be greater than zero");
        }
        this.premium = premium;
    }

    public void setCoverageAmount(BigDecimal coverageAmount) {
        if (coverageAmount == null) {
            throw new IllegalArgumentException("coverageAmount cannot be null");
        }
        if (coverageAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("coverageAmount must be greater than zero");
        }
        this.coverageAmount = coverageAmount;
    }

    public void setStatus(PolicyStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("status cannot be null");
        }
        this.status = status;
    }

    public void setType(InsuranceType type) {
        if (type == null) {
            throw new IllegalArgumentException("type cannot be null");
        }
        this.type = type;
    }

    public void validate() {
        if (policyNumber == null || policyNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("policyNumber is required");
        }
        if (startDate == null) {
            throw new IllegalArgumentException("startDate is required");
        }
        if (endDate == null) {
            throw new IllegalArgumentException("endDate is required");
        }
        if (premium == null) {
            throw new IllegalArgumentException("premium is required");
        }
        if (coverageAmount == null) {
            throw new IllegalArgumentException("coverageAmount is required");
        }
        if (status == null) {
            throw new IllegalArgumentException("status is required");
        }
        if (type == null) {
            throw new IllegalArgumentException("type is required");
        }
    }
}