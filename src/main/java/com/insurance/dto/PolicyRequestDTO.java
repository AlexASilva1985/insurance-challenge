package com.insurance.dto;

import com.insurance.domain.enums.InsuranceCategory;
import com.insurance.domain.enums.PaymentMethod;
import com.insurance.domain.enums.PolicyRequestStatus;
import com.insurance.domain.enums.SalesChannel;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.Data;

@Data
public class PolicyRequestDTO {
    private UUID id;
    
    @NotNull(message = "Customer ID is required")
    private UUID customerId;
    
    @NotNull(message = "Product ID is required")
    private UUID productId;
    
    @NotNull(message = "Category is required")
    private InsuranceCategory category;
    
    @NotNull(message = "Sales channel is required")
    private SalesChannel salesChannel;
    
    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;
    
    private PolicyRequestStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime finishedAt;
    
    @NotNull(message = "Total monthly premium amount is required")
    @Positive(message = "Total monthly premium amount must be positive")
    private BigDecimal totalMonthlyPremiumAmount;
    
    @NotNull(message = "Insured amount is required")
    @Positive(message = "Insured amount must be positive")
    private BigDecimal insuredAmount;
    
    @NotNull(message = "Coverages are required")
    private Map<String, BigDecimal> coverages = new HashMap<>();
    
    private List<String> assistances = new ArrayList<>();
    private List<StatusHistoryDTO> history;
} 