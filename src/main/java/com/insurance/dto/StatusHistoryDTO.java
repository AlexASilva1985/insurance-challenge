package com.insurance.dto;

import com.insurance.domain.enums.PolicyRequestStatus;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class StatusHistoryDTO {
    private PolicyRequestStatus status;
    private LocalDateTime timestamp;
} 