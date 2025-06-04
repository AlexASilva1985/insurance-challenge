package com.insurance.mapper;

import com.insurance.domain.PolicyRequest;
import com.insurance.domain.StatusHistory;
import com.insurance.dto.PolicyRequestDTO;
import com.insurance.dto.StatusHistoryDTO;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class PolicyRequestMapper {

    public PolicyRequest toEntity(PolicyRequestDTO dto) {
        if (dto == null) {
            return null;
        }

        PolicyRequest entity = new PolicyRequest();
        entity.setCustomerId(dto.getCustomerId());
        entity.setProductId(dto.getProductId());
        entity.setCategory(dto.getCategory());
        entity.setSalesChannel(dto.getSalesChannel());
        entity.setPaymentMethod(dto.getPaymentMethod());
        entity.setTotalMonthlyPremiumAmount(dto.getTotalMonthlyPremiumAmount());
        entity.setInsuredAmount(dto.getInsuredAmount());
        entity.setCoverages(dto.getCoverages());
        entity.setAssistances(dto.getAssistances());
        
        return entity;
    }

    public PolicyRequestDTO toDTO(PolicyRequest entity) {
        if (entity == null) {
            return null;
        }

        PolicyRequestDTO dto = new PolicyRequestDTO();
        dto.setId(entity.getId());
        dto.setCustomerId(entity.getCustomerId());
        dto.setProductId(entity.getProductId());
        dto.setCategory(entity.getCategory());
        dto.setSalesChannel(entity.getSalesChannel());
        dto.setPaymentMethod(entity.getPaymentMethod());
        dto.setStatus(entity.getStatus());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setFinishedAt(entity.getFinishedAt());
        dto.setTotalMonthlyPremiumAmount(entity.getTotalMonthlyPremiumAmount());
        dto.setInsuredAmount(entity.getInsuredAmount());
        dto.setCoverages(entity.getCoverages());
        dto.setAssistances(entity.getAssistances());
        
        if (entity.getStatusHistory() != null) {
            dto.setHistory(entity.getStatusHistory().stream()
                .map(this::toStatusHistoryDto)
                .collect(Collectors.toList()));
        }
        
        return dto;
    }

    private StatusHistoryDTO toStatusHistoryDto(StatusHistory entity) {
        if (entity == null) {
            return null;
        }

        StatusHistoryDTO dto = new StatusHistoryDTO();
        dto.setStatus(entity.getNewStatus());
        dto.setTimestamp(entity.getChangedAt());
        
        return dto;
    }
} 