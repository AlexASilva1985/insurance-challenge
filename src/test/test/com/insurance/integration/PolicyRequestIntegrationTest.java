package com.insurance.integration;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
class PolicyRequestIntegrationTest {

    /*
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private FraudAnalysisClient fraudAnalysisClient;

    @Test
    void shouldCreatePolicyRequestSuccessfully() throws Exception {
        PolicyRequestDTO request = createSamplePolicyRequest();
        
        mockMvc.perform(post("/api/v1/policy-requests")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("RECEIVED"))
                .andExpect(jsonPath("$.category").value("AUTO"))
                .andExpect(jsonPath("$.salesChannel").value("MOBILE"));
    }

    @Test
    void shouldProcessFraudAnalysisSuccessfully() throws Exception {
        PolicyRequestDTO request = createSamplePolicyRequest();
        
        FraudAnalysisResponse fraudResponse = new FraudAnalysisResponse();
        fraudResponse.setOrderId(UUID.randomUUID());
        fraudResponse.setCustomerId(request.getCustomerId());
        fraudResponse.setAnalyzedAt(LocalDateTime.now());
        fraudResponse.setOccurrences(new ArrayList<>());
        
        when(fraudAnalysisClient.analyzeFraud(any(), any())).thenReturn(fraudResponse);

        mockMvc.perform(post("/api/v1/policy-requests")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        // Processar análise de fraude
        mockMvc.perform(post("/api/v1/policy-requests/{id}/fraud-analysis", request.getId()))
                .andExpect(status().isOk());
    }

    private PolicyRequestDTO createSamplePolicyRequest() {
        PolicyRequestDTO request = new PolicyRequestDTO();
        request.setCustomerId(UUID.randomUUID());
        request.setProductId(UUID.randomUUID());
        request.setCategory(InsuranceCategory.AUTO);
        request.setSalesChannel(SalesChannel.MOBILE);
        request.setPaymentMethod(PaymentMethod.CREDIT_CARD);
        request.setTotalMonthlyPremiumAmount(new BigDecimal("75.25"));
        request.setInsuredAmount(new BigDecimal("275000.50"));
        request.setCoverages(new HashMap<>() {{
            put("Roubo", new BigDecimal("100000.25"));
            put("Perda Total", new BigDecimal("100000.25"));
            put("Colisão com Terceiros", new BigDecimal("75000.00"));
        }});
        request.setAssistances(new ArrayList<>() {{
            add("Guincho até 250km");
            add("Troca de Óleo");
            add("Chaveiro 24h");
        }});
        return request;
    }

     */
} 