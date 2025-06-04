package com.insurance.repository;

import com.insurance.domain.PolicyRequest;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PolicyRequestRepository extends JpaRepository<PolicyRequest, UUID> {
    List<PolicyRequest> findByCustomerId(UUID customerId);
} 