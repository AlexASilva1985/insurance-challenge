package com.insurance.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.time.LocalDate;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CustomerTest {

    private Customer customer;
    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();

        customer = new Customer();
        customer.setName("João Silva");
        customer.setCpf("123.456.789-00");
        customer.setEmail("joao.silva@email.com");
        customer.setBirthDate(LocalDate.of(1990, 1, 1));
        customer.setAddress("Rua das Flores, 123");
        customer.setPhone("(11) 99999-9999");
    }

    @Test
    void testCreateCustomerWithValidData() {
        Set<ConstraintViolation<Customer>> violations = validator.validate(customer);
        assertTrue(violations.isEmpty());

        assertNotNull(customer);
        assertEquals("João Silva", customer.getName());
        assertEquals("123.456.789-00", customer.getCpf());
        assertEquals("joao.silva@email.com", customer.getEmail());
        assertEquals(LocalDate.of(1990, 1, 1), customer.getBirthDate());
        assertEquals("Rua das Flores, 123", customer.getAddress());
        assertEquals("(11) 99999-9999", customer.getPhone());
        assertTrue(customer.getPolicies().isEmpty());
    }

    @Test
    void testNotAllowEmptyName() {
        customer.setName("");
        Set<ConstraintViolation<Customer>> violations = validator.validate(customer);
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        
        ConstraintViolation<Customer> violation = violations.iterator().next();
        assertEquals("name", violation.getPropertyPath().toString());
    }

    @Test
    void testNotAllowNullName() {
        customer.setName(null);
        Set<ConstraintViolation<Customer>> violations = validator.validate(customer);
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        
        ConstraintViolation<Customer> violation = violations.iterator().next();
        assertEquals("name", violation.getPropertyPath().toString());
    }

    @Test
    void testNotAllowEmptyCpf() {
        customer.setCpf("");
        Set<ConstraintViolation<Customer>> violations = validator.validate(customer);
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        
        ConstraintViolation<Customer> violation = violations.iterator().next();
        assertEquals("cpf", violation.getPropertyPath().toString());
    }

    @Test
    void testNotAllowNullCpf() {
        customer.setCpf(null);
        Set<ConstraintViolation<Customer>> violations = validator.validate(customer);
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        
        ConstraintViolation<Customer> violation = violations.iterator().next();
        assertEquals("cpf", violation.getPropertyPath().toString());
    }

    @Test
    void testValidateEmailFormat() {
        customer.setEmail("invalid-email");
        Set<ConstraintViolation<Customer>> violations = validator.validate(customer);
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        
        ConstraintViolation<Customer> violation = violations.iterator().next();
        assertEquals("email", violation.getPropertyPath().toString());
    }

    @Test
    void testAllowNullEmail() {
        customer.setEmail(null);
        Set<ConstraintViolation<Customer>> violations = validator.validate(customer);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testManagePolicies() {
        InsurancePolicy policy = new InsurancePolicy();
        policy.setCustomer(customer);
        customer.getPolicies().add(policy);

        assertEquals(1, customer.getPolicies().size());
        assertTrue(customer.getPolicies().contains(policy));

        customer.getPolicies().remove(policy);
        assertTrue(customer.getPolicies().isEmpty());
    }

    @Test
    void testInitializePoliciesCollection() {
        Customer newCustomer = new Customer();
        assertNotNull(newCustomer.getPolicies());
        assertTrue(newCustomer.getPolicies().isEmpty());
    }

    @Test
    void testCustomerWithMinimalValidData() {
        Customer customer = new Customer();
        customer.setName("João Silva");
        customer.setCpf("123.456.789-00");
        customer.setEmail("joao@example.com");

        assertEquals("João Silva", customer.getName());
        assertEquals("123.456.789-00", customer.getCpf());
        assertEquals("joao@example.com", customer.getEmail());
        assertNotNull(customer.getPolicies());
        assertTrue(customer.getPolicies().isEmpty());
    }

    @Test
    void testCustomerWithAllData() {
        Customer customer = new Customer();
        customer.setName("Maria Santos");
        customer.setCpf("987.654.321-00");
        customer.setEmail("maria@example.com");
        customer.setBirthDate(LocalDate.of(1990, 5, 15));
        customer.setAddress("Rua das Flores, 123");
        customer.setPhone("(11) 99999-9999");

        assertEquals("Maria Santos", customer.getName());
        assertEquals("987.654.321-00", customer.getCpf());
        assertEquals("maria@example.com", customer.getEmail());
        assertEquals(LocalDate.of(1990, 5, 15), customer.getBirthDate());
        assertEquals("Rua das Flores, 123", customer.getAddress());
        assertEquals("(11) 99999-9999", customer.getPhone());
    }

    @Test
    void testAddPolicyToCustomer() {
        Customer customer = new Customer();
        customer.setName("Pedro Costa");
        customer.setCpf("111.222.333-44");
        customer.setEmail("pedro@example.com");

        InsurancePolicy policy = new InsurancePolicy();
        policy.setPolicyNumber("POL-001");
        policy.setCustomer(customer);
        
        customer.getPolicies().add(policy);

        assertEquals(1, customer.getPolicies().size());
        assertTrue(customer.getPolicies().contains(policy));
    }

    @Test
    void testAddMultiplePoliciesToCustomer() {
        Customer customer = new Customer();
        customer.setName("Ana Silva");
        customer.setCpf("555.666.777-88");
        customer.setEmail("ana@example.com");

        InsurancePolicy policy1 = new InsurancePolicy();
        policy1.setPolicyNumber("POL-001");
        policy1.setCustomer(customer);
        
        InsurancePolicy policy2 = new InsurancePolicy();
        policy2.setPolicyNumber("POL-002");
        policy2.setCustomer(customer);
        
        customer.getPolicies().add(policy1);
        customer.getPolicies().add(policy2);

        assertEquals(2, customer.getPolicies().size());
        assertTrue(customer.getPolicies().contains(policy1));
        assertTrue(customer.getPolicies().contains(policy2));
    }

    @Test
    void testRemovePolicyFromCustomer() {
        Customer customer = new Customer();
        customer.setName("Carlos Lima");
        customer.setCpf("999.888.777-66");
        customer.setEmail("carlos@example.com");

        InsurancePolicy policy = new InsurancePolicy();
        policy.setPolicyNumber("POL-003");
        policy.setCustomer(customer);
        
        customer.getPolicies().add(policy);
        assertEquals(1, customer.getPolicies().size());
        
        customer.getPolicies().remove(policy);
        assertEquals(0, customer.getPolicies().size());
        assertFalse(customer.getPolicies().contains(policy));
    }

    @Test
    void testCustomerWithEmptyPoliciesSet() {
        Customer customer = new Customer();
        customer.setName("Teste User");
        customer.setCpf("123.123.123-12");
        customer.setEmail("teste@example.com");

        assertNotNull(customer.getPolicies());
        assertTrue(customer.getPolicies().isEmpty());
        assertEquals(0, customer.getPolicies().size());
    }

    @Test
    void testCustomerWithLongName() {
        Customer customer = new Customer();
        String longName = "Nome Muito Longo Com Várias Palavras Para Testar Limites do Campo Nome";
        customer.setName(longName);
        customer.setCpf("111.111.111-11");
        customer.setEmail("longo@example.com");

        assertEquals(longName, customer.getName());
    }

    @Test
    void testCustomerWithSpecialCharactersInName() {
        Customer customer = new Customer();
        String nameWithSpecialChars = "José da Silva Ção";
        customer.setName(nameWithSpecialChars);
        customer.setCpf("222.222.222-22");
        customer.setEmail("jose@example.com");

        assertEquals(nameWithSpecialChars, customer.getName());
    }

    @Test
    void testCustomerWithDifferentEmailFormats() {
        Customer customer = new Customer();
        customer.setName("Email Tester");
        customer.setCpf("333.333.333-33");
        
        // Test simple email
        customer.setEmail("simple@test.com");
        assertEquals("simple@test.com", customer.getEmail());
        
        // Test complex email
        customer.setEmail("complex.email+tag@subdomain.example.org");
        assertEquals("complex.email+tag@subdomain.example.org", customer.getEmail());
    }

    @Test
    void testCustomerWithDifferentCpfFormats() {
        Customer customer = new Customer();
        customer.setName("CPF Tester");
        customer.setEmail("cpf@test.com");
        
        // Test with dots and dash
        customer.setCpf("123.456.789-00");
        assertEquals("123.456.789-00", customer.getCpf());
        
        // Test without formatting
        customer.setCpf("12345678900");
        assertEquals("12345678900", customer.getCpf());
    }

    @Test
    void testCustomerWithDifferentPhoneFormats() {
        Customer customer = new Customer();
        customer.setName("Phone Tester");
        customer.setCpf("444.444.444-44");
        customer.setEmail("phone@test.com");
        
        // Test with parentheses and dash
        customer.setPhone("(11) 99999-9999");
        assertEquals("(11) 99999-9999", customer.getPhone());
        
        // Test simple format
        customer.setPhone("11999999999");
        assertEquals("11999999999", customer.getPhone());
        
        // Test with country code
        customer.setPhone("+55 11 99999-9999");
        assertEquals("+55 11 99999-9999", customer.getPhone());
    }

    @Test
    void testCustomerWithDifferentAddressFormats() {
        Customer customer = new Customer();
        customer.setName("Address Tester");
        customer.setCpf("555.555.555-55");
        customer.setEmail("address@test.com");
        
        // Test simple address
        customer.setAddress("Rua A, 123");
        assertEquals("Rua A, 123", customer.getAddress());
        
        // Test complex address
        String complexAddress = "Rua das Flores, 123, Apto 45, Jardim das Rosas, São Paulo - SP";
        customer.setAddress(complexAddress);
        assertEquals(complexAddress, customer.getAddress());
    }

    @Test
    void testCustomerWithBirthDateInPast() {
        Customer customer = new Customer();
        customer.setName("Birth Date Tester");
        customer.setCpf("666.666.666-66");
        customer.setEmail("birth@test.com");
        
        LocalDate pastDate = LocalDate.of(1980, 12, 25);
        customer.setBirthDate(pastDate);
        assertEquals(pastDate, customer.getBirthDate());
    }

    @Test
    void testCustomerWithBirthDateRecent() {
        Customer customer = new Customer();
        customer.setName("Young Customer");
        customer.setCpf("777.777.777-77");
        customer.setEmail("young@test.com");
        
        LocalDate recentDate = LocalDate.of(2000, 1, 1);
        customer.setBirthDate(recentDate);
        assertEquals(recentDate, customer.getBirthDate());
    }

    @Test
    void testCustomerAge() {
        Customer customer = new Customer();
        customer.setName("Age Calculator");
        customer.setCpf("888.888.888-88");
        customer.setEmail("age@test.com");
        
        // Set birth date 30 years ago
        LocalDate birthDate = LocalDate.now().minusYears(30);
        customer.setBirthDate(birthDate);
        
        assertEquals(birthDate, customer.getBirthDate());
        // Note: We're not testing age calculation since it's not implemented in the domain
    }

    @Test
    void testInheritanceFromBaseEntity() {
        assertTrue(customer instanceof BaseEntity);
        
        // Test inherited methods
        customer.setCreatedBy("admin");
        customer.setUpdatedBy("system");
        
        assertEquals("admin", customer.getCreatedBy());
        assertEquals("system", customer.getUpdatedBy());
    }
} 