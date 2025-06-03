package com.insurance.controller;

import com.insurance.dto.PolicyRequestDTO;
import com.insurance.mapper.PolicyRequestMapper;
import com.insurance.domain.PolicyRequest;
import com.insurance.service.PolicyRequestService;
import io.micrometer.core.annotation.Timed;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/policy-requests")
@RequiredArgsConstructor
public class PolicyRequestController {

    private static final Logger log = LoggerFactory.getLogger(PolicyRequestController.class);
    private final PolicyRequestService service;
    private final PolicyRequestMapper mapper;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Timed(value = "policy.request.create", description = "Time taken to create a policy request")
    public PolicyRequestDTO createPolicyRequest(@Valid @RequestBody PolicyRequestDTO request) {
        log.info("Creating policy request for customer: {}", request.getCustomerId());
        PolicyRequest entity = mapper.toEntity(request);
        PolicyRequest created = service.createPolicyRequest(entity);
        return mapper.toDTO(created);
    }

    @GetMapping("/{id}")
    @Timed(value = "policy.request.get", description = "Time taken to get a policy request")
    public PolicyRequestDTO getPolicyRequest(@PathVariable UUID id) {
        log.info("Getting policy request: {}", id);
        try {
            PolicyRequest entity = service.findById(id);
            return mapper.toDTO(entity);
        } catch (EntityNotFoundException e) {
            throw new EntityNotFoundException("Policy request not found with id: " + id);
        }
    }

    @GetMapping("/customer/{customerId}")
    @Timed(value = "policy.request.get.by.customer", description = "Time taken to get policy requests by customer")
    public List<PolicyRequestDTO> getPolicyRequestsByCustomer(@PathVariable UUID customerId) {
        log.info("Getting policy requests for customer: {}", customerId);
        return service.findByCustomerId(customerId).stream()
                .map(mapper::toDTO)
                .collect(Collectors.toList());
    }

    @PostMapping("/{id}/validate")
    public ResponseEntity<Void> validate(@PathVariable UUID id) {
        service.validatePolicyRequest(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/fraud-analysis")
    @Timed(value = "policy.request.fraud.analysis", description = "Time taken to process fraud analysis")
    public ResponseEntity<Void> processFraudAnalysis(@PathVariable UUID id) {
        log.info("Starting fraud analysis for policy request: {}", id);
        service.processFraudAnalysis(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/payment")
    @Timed(value = "policy.request.payment", description = "Time taken to process payment")
    public ResponseEntity<Void> processPayment(@PathVariable UUID id) {
        log.info("Processing payment for policy request: {}", id);
        service.processPayment(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/subscription")
    @Timed(value = "policy.request.subscription", description = "Time taken to process subscription")
    public ResponseEntity<Void> processSubscription(@PathVariable UUID id) {
        log.info("Processing subscription for policy request: {}", id);
        service.processSubscription(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/cancel")
    @Timed(value = "policy.request.cancel", description = "Time taken to cancel a policy request")
    public ResponseEntity<Void> cancelPolicyRequest(@PathVariable UUID id) {
        log.info("Canceling policy request: {}", id);
        service.cancelPolicyRequest(id);
        return ResponseEntity.ok().build();
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ProblemDetail> handleIllegalState(IllegalStateException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
            HttpStatus.UNPROCESSABLE_ENTITY,
            ex.getMessage()
        );
        problem.setTitle("Invalid State Transition");
        return ResponseEntity
            .status(HttpStatus.UNPROCESSABLE_ENTITY)
            .contentType(MediaType.APPLICATION_PROBLEM_JSON)
            .body(problem);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleEntityNotFound(EntityNotFoundException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
            HttpStatus.NOT_FOUND,
            ex.getMessage()
        );
        problem.setTitle("Resource Not Found");
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .contentType(MediaType.APPLICATION_PROBLEM_JSON)
            .body(problem);
    }
} 