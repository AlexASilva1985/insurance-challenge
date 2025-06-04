package com.insurance.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "risk_occurrences")
@Data
@EqualsAndHashCode(callSuper = true)
public class RiskOccurrence extends BaseEntity {

    @Column(nullable = false)
    private String type;

    @Column(nullable = false)
    private String description;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public void setType(String type) {
        if (type == null || type.trim().isEmpty()) {
            throw new IllegalArgumentException("type cannot be empty");
        }
        this.type = type;
    }

    public void setDescription(String description) {
        if (description == null || description.trim().isEmpty()) {
            throw new IllegalArgumentException("description cannot be empty");
        }
        this.description = description;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        if (createdAt == null) {
            throw new IllegalArgumentException("createdAt cannot be null");
        }
        if (createdAt.isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("createdAt cannot be a future date");
        }
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        if (updatedAt == null) {
            throw new IllegalArgumentException("updatedAt cannot be null");
        }
        if (updatedAt.isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("updatedAt cannot be a future date");
        }
        if (createdAt != null && updatedAt.isBefore(createdAt)) {
            throw new IllegalArgumentException("updatedAt cannot be before createdAt");
        }
        this.updatedAt = updatedAt;
    }

    public void validate() {
        if (type == null || type.trim().isEmpty()) {
            throw new IllegalArgumentException("type is required");
        }
        if (description == null || description.trim().isEmpty()) {
            throw new IllegalArgumentException("description is required");
        }
        if (createdAt == null) {
            throw new IllegalArgumentException("createdAt is required");
        }
        if (updatedAt == null) {
            throw new IllegalArgumentException("updatedAt is required");
        }
    }

    @PrePersist
    public void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = createdAt;
        }
    }

    @PreUpdate
    public void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
} 