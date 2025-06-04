package com.insurance.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class BaseEntityTest {

    private TestEntity testEntity;

    @BeforeEach
    void setUp() {
        testEntity = new TestEntity();
    }

    @Test
    void testIdGeneration() {
        UUID id = UUID.randomUUID();
        testEntity.setId(id);
        assertEquals(id, testEntity.getId());
    }

    @Test
    void testCreatedAtHandling() {
        LocalDateTime now = LocalDateTime.now();
        testEntity.setCreatedAt(now);
        assertEquals(now, testEntity.getCreatedAt());
    }

    @Test
    void testUpdatedAtHandling() {
        LocalDateTime now = LocalDateTime.now();
        testEntity.setUpdatedAt(now);
        assertEquals(now, testEntity.getUpdatedAt());
    }

    @Test
    void testCreatedByHandling() {
        String createdBy = "admin";
        testEntity.setCreatedBy(createdBy);
        assertEquals(createdBy, testEntity.getCreatedBy());
    }

    @Test
    void testUpdatedByHandling() {
        String updatedBy = "system";
        testEntity.setUpdatedBy(updatedBy);
        assertEquals(updatedBy, testEntity.getUpdatedBy());
    }

    @Test
    void testDefaultValues() {
        TestEntity entity = new TestEntity();
        assertNull(entity.getId());
        assertNull(entity.getCreatedAt());
        assertNull(entity.getUpdatedAt());
        assertNull(entity.getCreatedBy());
        assertNull(entity.getUpdatedBy());
    }

    @Test
    void testEqualsAndHashCode() {
        TestEntity entity1 = new TestEntity();
        TestEntity entity2 = new TestEntity();

        UUID id = UUID.randomUUID();
        entity1.setId(id);
        entity2.setId(id);

        assertEquals(entity1, entity2);
        assertEquals(entity1.hashCode(), entity2.hashCode());

        entity2.setId(UUID.randomUUID());
        assertNotEquals(entity1, entity2);
    }

    @Test
    void testToString() {
        testEntity.setId(UUID.randomUUID());
        testEntity.setCreatedBy("admin");
        testEntity.setUpdatedBy("system");

        String toString = testEntity.toString();
        assertNotNull(toString);
        assertTrue(toString.length() > 0);
        assertTrue(toString.contains("admin") || toString.contains("system"));
    }

    @Test
    void testAuditFields() {
        String createdBy = "user1";
        String updatedBy = "user2";
        LocalDateTime createdAt = LocalDateTime.now().minusDays(1);
        LocalDateTime updatedAt = LocalDateTime.now();

        testEntity.setCreatedBy(createdBy);
        testEntity.setUpdatedBy(updatedBy);
        testEntity.setCreatedAt(createdAt);
        testEntity.setUpdatedAt(updatedAt);

        assertEquals(createdBy, testEntity.getCreatedBy());
        assertEquals(updatedBy, testEntity.getUpdatedBy());
        assertEquals(createdAt, testEntity.getCreatedAt());
        assertEquals(updatedAt, testEntity.getUpdatedAt());
    }

    @Test
    void testNullAuditFields() {
        testEntity.setCreatedBy(null);
        testEntity.setUpdatedBy(null);
        testEntity.setCreatedAt(null);
        testEntity.setUpdatedAt(null);

        assertNull(testEntity.getCreatedBy());
        assertNull(testEntity.getUpdatedBy());
        assertNull(testEntity.getCreatedAt());
        assertNull(testEntity.getUpdatedAt());
    }

    // Test entity class for testing BaseEntity
    private static class TestEntity extends BaseEntity {
        // No additional fields needed for testing
    }
} 