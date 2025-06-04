package com.insurance.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.insurance.domain.PolicyRequest;
import com.insurance.domain.enums.InsuranceCategory;
import com.insurance.domain.enums.PaymentMethod;
import com.insurance.domain.enums.PolicyRequestStatus;
import com.insurance.domain.enums.SalesChannel;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

@DataJpaTest
class PolicyRequestRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private PolicyRequestRepository repository;

    private UUID customerId;
    private PolicyRequest policyRequest;

    @BeforeEach
    void setUp() {
        customerId = UUID.randomUUID();
        policyRequest = createSamplePolicyRequest(customerId);
    }

    @Test
    void testSavePolicyRequest() {
        PolicyRequest savedRequest = repository.save(policyRequest);
        
        assertNotNull(savedRequest.getId());
        assertEquals(customerId, savedRequest.getCustomerId());
        assertEquals(InsuranceCategory.AUTO, savedRequest.getCategory());
        assertEquals(PolicyRequestStatus.RECEIVED, savedRequest.getStatus());
    }

    @Test
    void testFindById() {
        PolicyRequest savedRequest = entityManager.persistAndFlush(policyRequest);
        
        Optional<PolicyRequest> found = repository.findById(savedRequest.getId());
        
        assertTrue(found.isPresent());
        assertEquals(savedRequest.getId(), found.get().getId());
        assertEquals(customerId, found.get().getCustomerId());
    }

    @Test
    void testFindByCustomerId() {
        // Criar e persistir múltiplas solicitações para o mesmo cliente
        PolicyRequest request1 = createSamplePolicyRequest(customerId);
        PolicyRequest request2 = createSamplePolicyRequest(customerId);
        PolicyRequest otherCustomerRequest = createSamplePolicyRequest(UUID.randomUUID());
        
        entityManager.persist(request1);
        entityManager.persist(request2);
        entityManager.persist(otherCustomerRequest);
        entityManager.flush();

        List<PolicyRequest> foundRequests = repository.findByCustomerId(customerId);
        
        assertEquals(2, foundRequests.size());
        assertTrue(foundRequests.stream().allMatch(r -> r.getCustomerId().equals(customerId)));
    }

    @Test
    void testDeletePolicyRequest() {
        PolicyRequest savedRequest = entityManager.persistAndFlush(policyRequest);
        
        repository.deleteById(savedRequest.getId());
        
        Optional<PolicyRequest> found = repository.findById(savedRequest.getId());
        assertFalse(found.isPresent());
    }

    @Test
    void testUpdatePolicyRequest() {
        // Persistir a solicitação original
        PolicyRequest savedRequest = entityManager.persistAndFlush(policyRequest);
        UUID savedId = savedRequest.getId();
        
        // Buscar a entidade persistida
        PolicyRequest foundRequest = repository.findById(savedId).orElseThrow();
        
        // Atualizar os campos necessários
        foundRequest.setStatus(PolicyRequestStatus.VALIDATED);
        foundRequest.setUpdatedAt(LocalDateTime.now());
        
        // Criar novas instâncias das coleções
        HashMap<String, BigDecimal> newCoverages = new HashMap<>(foundRequest.getCoverages());
        List<String> newAssistances = new ArrayList<>(foundRequest.getAssistances());
        
        foundRequest.setCoverages(newCoverages);
        foundRequest.setAssistances(newAssistances);
        
        // Salvar a atualização
        repository.save(foundRequest);
        entityManager.flush();
        entityManager.clear();
        
        // Verificar a atualização
        PolicyRequest found = entityManager.find(PolicyRequest.class, savedId);
        assertEquals(PolicyRequestStatus.VALIDATED, found.getStatus());
    }

    @Test
    void testFindByCustomerIdWithNoResults() {
        UUID nonExistentCustomerId = UUID.randomUUID();
        List<PolicyRequest> foundRequests = repository.findByCustomerId(nonExistentCustomerId);
        
        assertTrue(foundRequests.isEmpty());
    }

    private PolicyRequest createSamplePolicyRequest(UUID customerId) {
        LocalDateTime now = LocalDateTime.now();
        PolicyRequest request = new PolicyRequest();
        request.setCustomerId(customerId);
        request.setProductId(UUID.randomUUID());
        request.setCategory(InsuranceCategory.AUTO);
        request.setSalesChannel(SalesChannel.MOBILE);
        request.setPaymentMethod(PaymentMethod.CREDIT_CARD);
        request.setStatus(PolicyRequestStatus.RECEIVED);
        request.setCreatedAt(now);
        request.setUpdatedAt(now);
        request.setCreatedBy("system");
        request.setUpdatedBy("system");
        request.setTotalMonthlyPremiumAmount(BigDecimal.valueOf(150.00));
        request.setInsuredAmount(BigDecimal.valueOf(50000.00));
        
        HashMap<String, BigDecimal> coverages = new HashMap<>();
        coverages.put("Collision", BigDecimal.valueOf(30000));
        coverages.put("Theft", BigDecimal.valueOf(20000));
        request.setCoverages(coverages);
        
        request.setAssistances(new ArrayList<>(Arrays.asList("Roadside Assistance", "Glass Protection")));
        
        return request;
    }
} 