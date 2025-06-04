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
} 