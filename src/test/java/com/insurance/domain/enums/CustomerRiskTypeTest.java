package com.insurance.domain.enums;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class CustomerRiskTypeTest {

    @Test
    void testAllRiskTypesExist() {
        CustomerRiskType[] riskTypes = CustomerRiskType.values();
        assertEquals(4, riskTypes.length);
    }

    @Test
    void testSpecificRiskTypeValues() {
        assertEquals("REGULAR", CustomerRiskType.REGULAR.name());
        assertEquals("HIGH_RISK", CustomerRiskType.HIGH_RISK.name());
        assertEquals("PREFERRED", CustomerRiskType.PREFERRED.name());
        assertEquals("NO_INFORMATION", CustomerRiskType.NO_INFORMATION.name());
    }

    @Test
    void testValueOf() {
        assertEquals(CustomerRiskType.REGULAR, CustomerRiskType.valueOf("REGULAR"));
        assertEquals(CustomerRiskType.HIGH_RISK, CustomerRiskType.valueOf("HIGH_RISK"));
        assertEquals(CustomerRiskType.PREFERRED, CustomerRiskType.valueOf("PREFERRED"));
        assertEquals(CustomerRiskType.NO_INFORMATION, CustomerRiskType.valueOf("NO_INFORMATION"));
    }

    @Test
    void testEnumOrder() {
        CustomerRiskType[] riskTypes = CustomerRiskType.values();
        assertEquals(CustomerRiskType.REGULAR, riskTypes[0]);
        assertEquals(CustomerRiskType.HIGH_RISK, riskTypes[1]);
        assertEquals(CustomerRiskType.PREFERRED, riskTypes[2]);
        assertEquals(CustomerRiskType.NO_INFORMATION, riskTypes[3]);
    }

    @Test
    void testToString() {
        for (CustomerRiskType riskType : CustomerRiskType.values()) {
            assertNotNull(riskType.toString());
            assertTrue(riskType.toString().length() > 0);
        }
    }

    @Test
    void testEquality() {
        assertEquals(CustomerRiskType.REGULAR, CustomerRiskType.REGULAR);
        assertEquals(CustomerRiskType.HIGH_RISK, CustomerRiskType.HIGH_RISK);
        assertEquals(CustomerRiskType.PREFERRED, CustomerRiskType.PREFERRED);
        assertEquals(CustomerRiskType.NO_INFORMATION, CustomerRiskType.NO_INFORMATION);
    }

    @Test
    void testHashCode() {
        for (CustomerRiskType riskType : CustomerRiskType.values()) {
            assertNotNull(riskType.hashCode());
        }
    }

    @Test
    void testOrdinalValues() {
        assertEquals(0, CustomerRiskType.REGULAR.ordinal());
        assertEquals(1, CustomerRiskType.HIGH_RISK.ordinal());
        assertEquals(2, CustomerRiskType.PREFERRED.ordinal());
        assertEquals(3, CustomerRiskType.NO_INFORMATION.ordinal());
    }
} 