package com.example.awsdemo;

import com.example.awsdemo.integration.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;

class AwsDemoApplicationTests extends AbstractIntegrationTest {

    @Test
    void contextLoads() {
        // Passing means Flyway migrations ran and Hibernate's schema validation
        // accepted the entity mappings against a real Postgres.
    }
}
