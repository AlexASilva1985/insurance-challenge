package com.insurance.domain;

import com.insurance.domain.enums.CustomerRiskType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "risk_analysis")
@Data
@EqualsAndHashCode(callSuper = true)
public class RiskAnalysis extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CustomerRiskType classification;

    @Column(nullable = false)
    private LocalDateTime analyzedAt;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "risk_analysis_id")
    private List<RiskOccurrence> occurrences = new ArrayList<>();
} 