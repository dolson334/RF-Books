package com.rfbooks.config;

import org.springframework.boot.test.context.TestConfiguration;

/**
 * Marker config for tests. The main multitenancy beans (HibernateConfig,
 * SchemaBasedMultiTenantConnectionProvider, CurrentTenantIdentifierResolverImpl)
 * are excluded via @ConditionalOnProperty(rfbooks.multitenancy.enabled=true)
 * and the test profile sets it to false, so Spring Boot auto-configures JPA with H2.
 */
@TestConfiguration
public class TestHibernateConfig {
}
