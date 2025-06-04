package com.insurance.domain;

import com.insurance.domain.enums.CustomerRiskType;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "risk_analysis")
@Data
@EqualsAndHashCode(callSuper = true)
public class RiskAnalysis extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CustomerRiskType classification;

    @Column(name = "analyzed_at", nullable = false)
    private LocalDateTime analyzedAt;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "risk_analysis_id")
    private List<RiskOccurrence> occurrences = new ArrayList<>();

    public void validate() {
        if (classification == null) {
            throw new IllegalArgumentException("classification is required");
        }
        if (analyzedAt == null) {
            throw new IllegalArgumentException("analyzedAt is required");
        }
        if (analyzedAt.isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("analyzedAt cannot be a future date");
        }
    }

    public void setClassification(CustomerRiskType classification) {
        if (classification == null) {
            throw new IllegalArgumentException("classification cannot be null");
        }
        this.classification = classification;
    }

    public void setAnalyzedAt(LocalDateTime analyzedAt) {
        if (analyzedAt == null) {
            throw new IllegalArgumentException("analyzedAt cannot be null");
        }
        if (analyzedAt.isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("analyzedAt cannot be a future date");
        }
        this.analyzedAt = analyzedAt;
    }

    public void addOccurrence(RiskOccurrence occurrence) {
        if (occurrence == null) {
            throw new IllegalArgumentException("occurrence cannot be null");
        }
        occurrence.validate();
        this.occurrences.add(occurrence);
    }

    public void removeOccurrence(RiskOccurrence occurrence) {
        if (occurrence == null) {
            throw new IllegalArgumentException("occurrence cannot be null");
        }
        this.occurrences.remove(occurrence);
    }
} 