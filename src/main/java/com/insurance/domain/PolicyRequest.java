package com.insurance.domain;

import com.insurance.domain.enums.InsuranceCategory;
import com.insurance.domain.enums.PaymentMethod;
import com.insurance.domain.enums.PolicyRequestStatus;
import com.insurance.domain.enums.SalesChannel;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

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
    private PolicyRequestStatus status;

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
    @OrderBy("createdAt DESC")
    private List<StatusHistory> statusHistory = new ArrayList<>();

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "risk_analysis_id")
    private RiskAnalysis riskAnalysis;

    @Column(name = "finished_at")
    private LocalDateTime finishedAt;

    public void addToHistory(PolicyRequestStatus newStatus) {
        StatusHistory history = new StatusHistory();
        history.setFromStatus(this.status);
        history.setToStatus(newStatus);
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
} 