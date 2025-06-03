package com.insurance.dto;

import com.insurance.domain.enums.PolicyRequestStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class StatusHistoryDTO {
    private PolicyRequestStatus status;
    private LocalDateTime timestamp;
} 