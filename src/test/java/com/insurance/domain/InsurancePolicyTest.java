package com.insurance.domain;

import com.insurance.domain.enums.InsuranceType;
import com.insurance.domain.enums.PolicyStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class InsurancePolicyTest {

    private InsurancePolicy policy;
    private LocalDate today;
    private LocalDate tomorrow;
    private LocalDate yesterday;

    @BeforeEach
    void setUp() {
        policy = new InsurancePolicy();
        today = LocalDate.now();
        tomorrow = today.plusDays(1);
        yesterday = today.minusDays(1);
    }

    @Test
    void testCreateValidPolicy() {
        policy.setPolicyNumber("POL-12345");
        policy.setStartDate(today);
        policy.setEndDate(tomorrow);
        policy.setPremium(BigDecimal.valueOf(1000));
        policy.setCoverageAmount(BigDecimal.valueOf(50000));
        policy.setStatus(PolicyStatus.ACTIVE);
        policy.setType(InsuranceType.AUTO);

        policy.validate();

        assertEquals("POL-12345", policy.getPolicyNumber());
        assertEquals(today, policy.getStartDate());
        assertEquals(tomorrow, policy.getEndDate());
        assertEquals(BigDecimal.valueOf(1000), policy.getPremium());
        assertEquals(BigDecimal.valueOf(50000), policy.getCoverageAmount());
        assertEquals(PolicyStatus.ACTIVE, policy.getStatus());
        assertEquals(InsuranceType.AUTO, policy.getType());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "  "})
    void testNotAllowInvalidPolicyNumber(String invalidNumber) {
        assertThrows(IllegalArgumentException.class,
            () -> policy.setPolicyNumber(invalidNumber));
    }

    @Test
    void testNotAllowPolicyNumberWithoutPrefix() {
        assertThrows(IllegalArgumentException.class,
            () -> policy.setPolicyNumber("12345"));
    }

    @Test
    void testNotAllowNullStartDate() {
        assertThrows(IllegalArgumentException.class,
            () -> policy.setStartDate(null));
    }

    @Test
    void testNotAllowStartDateAfterEndDate() {
        policy.setEndDate(today);
        assertThrows(IllegalArgumentException.class,
            () -> policy.setStartDate(tomorrow));
    }

    @Test
    void testNotAllowNullEndDate() {
        assertThrows(IllegalArgumentException.class,
            () -> policy.setEndDate(null));
    }

    @Test
    void testNotAllowEndDateBeforeStartDate() {
        policy.setStartDate(today);
        assertThrows(IllegalArgumentException.class,
            () -> policy.setEndDate(yesterday));
    }

    @Test
    void testNotAllowNullPremium() {
        assertThrows(IllegalArgumentException.class,
            () -> policy.setPremium(null));
    }

    @Test
    void testNotAllowZeroPremium() {
        assertThrows(IllegalArgumentException.class,
            () -> policy.setPremium(BigDecimal.ZERO));
    }

    @Test
    void testNotAllowNegativePremium() {
        assertThrows(IllegalArgumentException.class,
            () -> policy.setPremium(BigDecimal.valueOf(-1)));
    }

    @Test
    void testNotAllowNullCoverageAmount() {
        assertThrows(IllegalArgumentException.class,
            () -> policy.setCoverageAmount(null));
    }

    @Test
    void testNotAllowZeroCoverageAmount() {
        assertThrows(IllegalArgumentException.class,
            () -> policy.setCoverageAmount(BigDecimal.ZERO));
    }

    @Test
    void testNotAllowNegativeCoverageAmount() {
        assertThrows(IllegalArgumentException.class,
            () -> policy.setCoverageAmount(BigDecimal.valueOf(-1)));
    }

    @Test
    void testNotAllowNullStatus() {
        assertThrows(IllegalArgumentException.class,
            () -> policy.setStatus(null));
    }

    @Test
    void testNotAllowNullType() {
        assertThrows(IllegalArgumentException.class,
            () -> policy.setType(null));
    }

    @Test
    void testValidateExceptionWhenPolicyNumberIsNull() {
        policy.setStartDate(today);
        policy.setEndDate(tomorrow);
        policy.setPremium(BigDecimal.valueOf(1000));
        policy.setCoverageAmount(BigDecimal.valueOf(50000));
        policy.setStatus(PolicyStatus.ACTIVE);
        policy.setType(InsuranceType.AUTO);

        assertThrows(IllegalArgumentException.class, () -> policy.validate());
    }

    @Test
    void testValidateExceptionWhenStartDateIsNull() {
        policy.setPolicyNumber("POL-12345");
        policy.setEndDate(tomorrow);
        policy.setPremium(BigDecimal.valueOf(1000));
        policy.setCoverageAmount(BigDecimal.valueOf(50000));
        policy.setStatus(PolicyStatus.ACTIVE);
        policy.setType(InsuranceType.AUTO);

        assertThrows(IllegalArgumentException.class, () -> policy.validate());
    }

    @Test
    void testValidateExceptionWhenEndDateIsNull() {
        policy.setPolicyNumber("POL-12345");
        policy.setStartDate(today);
        policy.setPremium(BigDecimal.valueOf(1000));
        policy.setCoverageAmount(BigDecimal.valueOf(50000));
        policy.setStatus(PolicyStatus.ACTIVE);
        policy.setType(InsuranceType.AUTO);

        assertThrows(IllegalArgumentException.class, () -> policy.validate());
    }

    @Test
    void testValidateExceptionWhenPremiumIsNull() {
        policy.setPolicyNumber("POL-12345");
        policy.setStartDate(today);
        policy.setEndDate(tomorrow);
        policy.setCoverageAmount(BigDecimal.valueOf(50000));
        policy.setStatus(PolicyStatus.ACTIVE);
        policy.setType(InsuranceType.AUTO);

        assertThrows(IllegalArgumentException.class, () -> policy.validate());
    }

    @Test
    void testValidateExceptionWhenCoverageAmountIsNull() {
        policy.setPolicyNumber("POL-12345");
        policy.setStartDate(today);
        policy.setEndDate(tomorrow);
        policy.setPremium(BigDecimal.valueOf(1000));
        policy.setStatus(PolicyStatus.ACTIVE);
        policy.setType(InsuranceType.AUTO);

        assertThrows(IllegalArgumentException.class, () -> policy.validate());
    }

    @Test
    void testValidateExceptionWhenStatusIsNull() {
        policy.setPolicyNumber("POL-12345");
        policy.setStartDate(today);
        policy.setEndDate(tomorrow);
        policy.setPremium(BigDecimal.valueOf(1000));
        policy.setCoverageAmount(BigDecimal.valueOf(50000));
        policy.setType(InsuranceType.AUTO);

        assertThrows(IllegalArgumentException.class, () -> policy.validate());
    }

    @Test
    void testValidateExceptionWhenTypeIsNull() {
        policy.setPolicyNumber("POL-12345");
        policy.setStartDate(today);
        policy.setEndDate(tomorrow);
        policy.setPremium(BigDecimal.valueOf(1000));
        policy.setCoverageAmount(BigDecimal.valueOf(50000));
        policy.setStatus(PolicyStatus.ACTIVE);

        assertThrows(IllegalArgumentException.class, () -> policy.validate());
    }
} 