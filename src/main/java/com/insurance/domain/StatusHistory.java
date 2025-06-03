package com.insurance.domain;

import com.insurance.domain.enums.PolicyRequestStatus;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Entity
@Table(name = "status_history")
@Data
@EqualsAndHashCode(callSuper = true)
public class StatusHistory extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PolicyRequestStatus fromStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PolicyRequestStatus toStatus;

    @Column(nullable = false)
    private LocalDateTime changedAt;

    private String reason;

    public PolicyRequestStatus getStatus() {
        return toStatus;
    }

    public LocalDateTime getTimestamp() {
        return changedAt;
    }
} 