package com.insurance.domain;

import com.insurance.domain.enums.InsuranceCategory;
import com.insurance.domain.enums.PaymentMethod;
import com.insurance.domain.enums.PolicyRequestStatus;
import com.insurance.domain.enums.SalesChannel;
import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "policy_requests")
@Data
@EqualsAndHashCode(callSuper = true)
public class PolicyRequest extends BaseEntity {

    @Column(nullable = false)
    private UUID customerId;

    @Column(nullable = false)
    private UUID productId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InsuranceCategory category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SalesChannel salesChannel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PolicyRequestStatus status = PolicyRequestStatus.RECEIVED;

    @Column(name = "total_monthly_premium_amount", nullable = false)
    private BigDecimal totalMonthlyPremiumAmount;

    @Column(name = "insured_amount", nullable = false)
    private BigDecimal insuredAmount;

    @ElementCollection
    @CollectionTable(name = "policy_request_coverages", 
                    joinColumns = @JoinColumn(name = "policy_request_id"))
    @MapKeyColumn(name = "coverage_name")
    @Column(name = "coverage_amount")
    private Map<String, BigDecimal> coverages = new HashMap<>();

    @ElementCollection
    @CollectionTable(name = "policy_request_assistances", 
                    joinColumns = @JoinColumn(name = "policy_request_id"))
    @Column(name = "assistance_name")
    private List<String> assistances = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "policy_request_id")
    @OrderBy("changedAt DESC")
    private List<StatusHistory> statusHistory = new ArrayList<>();

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "risk_analysis_id")
    private RiskAnalysis riskAnalysis;

    @Column(name = "finished_at")
    private LocalDateTime finishedAt;

    public void validate() {
        if (customerId == null) {
            throw new IllegalArgumentException("customerId is required");
        }
        if (productId == null) {
            throw new IllegalArgumentException("productId is required");
        }
        if (category == null) {
            throw new IllegalArgumentException("category is required");
        }
        if (salesChannel == null) {
            throw new IllegalArgumentException("salesChannel is required");
        }
        if (paymentMethod == null) {
            throw new IllegalArgumentException("paymentMethod is required");
        }
        if (totalMonthlyPremiumAmount == null || totalMonthlyPremiumAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("totalMonthlyPremiumAmount must be greater than zero");
        }
        if (insuredAmount == null || insuredAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("insuredAmount must be greater than zero");
        }
        if (coverages.isEmpty()) {
            throw new IllegalArgumentException("At least one coverage is required");
        }
    }

    public void updateStatus(PolicyRequestStatus newStatus) {
        if (newStatus == null) {
            throw new IllegalArgumentException("New status cannot be null");
        }

        if (!canTransitionTo(newStatus)) {
            throw new IllegalStateException("Invalid status transition from " + this.status + " to " + newStatus);
        }

        StatusHistory history = new StatusHistory();
        history.setPolicyRequestId(this.getId());
        history.setPreviousStatus(this.status);
        history.setNewStatus(newStatus);
        history.setChangedAt(LocalDateTime.now());
        
        this.statusHistory.add(history);
        this.status = newStatus;

        if (newStatus == PolicyRequestStatus.APPROVED || 
            newStatus == PolicyRequestStatus.REJECTED || 
            newStatus == PolicyRequestStatus.CANCELLED) {
            this.finishedAt = LocalDateTime.now();
        }
    }

    public boolean canTransitionTo(PolicyRequestStatus newStatus) {
        if (this.status == null) {
            return newStatus == PolicyRequestStatus.RECEIVED;
        }

        return switch (this.status) {
            case RECEIVED -> newStatus == PolicyRequestStatus.VALIDATED || 
                           newStatus == PolicyRequestStatus.REJECTED ||
                           newStatus == PolicyRequestStatus.CANCELLED;
            case VALIDATED -> newStatus == PolicyRequestStatus.PENDING || 
                            newStatus == PolicyRequestStatus.REJECTED ||
                            newStatus == PolicyRequestStatus.CANCELLED;
            case PENDING -> newStatus == PolicyRequestStatus.APPROVED || 
                          newStatus == PolicyRequestStatus.REJECTED ||
                          newStatus == PolicyRequestStatus.CANCELLED;
            case APPROVED -> false; // Não pode mudar após aprovado
            case REJECTED, CANCELLED -> false; // Estados finais
        };
    }

    public BigDecimal calculateTotalCoverageAmount() {
        return coverages.values()
                       .stream()
                       .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public void setTotalMonthlyPremiumAmount(BigDecimal totalMonthlyPremiumAmount) {
        if (totalMonthlyPremiumAmount == null || totalMonthlyPremiumAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("totalMonthlyPremiumAmount must be greater than zero");
        }
        this.totalMonthlyPremiumAmount = totalMonthlyPremiumAmount;
    }

    public void setInsuredAmount(BigDecimal insuredAmount) {
        if (insuredAmount == null || insuredAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("insuredAmount must be greater than zero");
        }
        this.insuredAmount = insuredAmount;
    }
} 