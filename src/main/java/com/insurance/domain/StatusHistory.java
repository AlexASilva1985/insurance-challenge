package com.insurance.domain;

import com.insurance.domain.enums.PolicyRequestStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "status_history")
@Data
@EqualsAndHashCode(callSuper = true)
public class StatusHistory extends BaseEntity {

    @Column(name = "policy_request_id", nullable = false)
    private UUID policyRequestId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PolicyRequestStatus previousStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PolicyRequestStatus newStatus;

    @Column(nullable = false)
    private LocalDateTime changedAt;

    private String reason;

    public void setPolicyRequestId(UUID policyRequestId) {
        if (policyRequestId == null) {
            throw new IllegalArgumentException("policyRequestId cannot be null");
        }
        this.policyRequestId = policyRequestId;
    }

    public void setPreviousStatus(PolicyRequestStatus previousStatus) {
        if (previousStatus == null) {
            throw new IllegalArgumentException("previousStatus cannot be null");
        }
        this.previousStatus = previousStatus;
    }

    public void setNewStatus(PolicyRequestStatus newStatus) {
        if (newStatus == null) {
            throw new IllegalArgumentException("newStatus cannot be null");
        }
        if (this.previousStatus != null && newStatus == this.previousStatus) {
            throw new IllegalArgumentException("newStatus cannot be the same as previousStatus");
        }
        if (this.previousStatus != null && !isValidStatusTransition(this.previousStatus, newStatus)) {
            throw new IllegalStateException("Invalid status transition from " + this.previousStatus + " to " + newStatus);
        }
        this.newStatus = newStatus;
    }

    private boolean isValidStatusTransition(PolicyRequestStatus from, PolicyRequestStatus to) {
        switch (from) {
            case RECEIVED:
                return to == PolicyRequestStatus.VALIDATED || 
                       to == PolicyRequestStatus.REJECTED ||
                       to == PolicyRequestStatus.CANCELLED;
            case VALIDATED:
                return to == PolicyRequestStatus.PENDING || 
                       to == PolicyRequestStatus.REJECTED ||
                       to == PolicyRequestStatus.CANCELLED;
            case PENDING:
                return to == PolicyRequestStatus.APPROVED || 
                       to == PolicyRequestStatus.REJECTED ||
                       to == PolicyRequestStatus.CANCELLED;
            case APPROVED:
                return false; // Não pode mudar após aprovado
            case REJECTED:
            case CANCELLED:
                return false; // Estados finais
            default:
                return false;
        }
    }

    public void validate() {
        if (policyRequestId == null) {
            throw new IllegalArgumentException("policyRequestId is required");
        }
        if (previousStatus == null) {
            throw new IllegalArgumentException("previousStatus is required");
        }
        if (newStatus == null) {
            throw new IllegalArgumentException("newStatus is required");
        }
        if (changedAt == null) {
            throw new IllegalArgumentException("changedAt is required");
        }
    }

    @PrePersist
    public void onCreate() {
        if (changedAt == null) {
            changedAt = LocalDateTime.now();
        }
    }
} 