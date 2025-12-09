package com.rfbooks.services;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;

@Service
public class TenantService {

    private final JdbcTemplate jdbcTemplate;

    public TenantService(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    /**
     * Create a new tenant schema
     */
    public void createTenantSchema(String tenantId) {
        String sanitizedTenantId = sanitizeTenantId(tenantId);
        
        // Create schema
        jdbcTemplate.execute("CREATE SCHEMA IF NOT EXISTS " + sanitizedTenantId);
        
        // Grant permissions
        jdbcTemplate.execute("GRANT ALL PRIVILEGES ON SCHEMA " + sanitizedTenantId + " TO postgres");
        
        System.out.println("Created tenant schema: " + sanitizedTenantId);
    }

    /**
     * Check if tenant schema exists
     */
    public boolean tenantExists(String tenantId) {
        String sanitizedTenantId = sanitizeTenantId(tenantId);
        Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM information_schema.schemata WHERE schema_name = ?",
            Integer.class,
            sanitizedTenantId
        );
        return count != null && count > 0;
    }

    /**
     * Delete a tenant schema (use with caution!)
     */
    public void deleteTenantSchema(String tenantId) {
        String sanitizedTenantId = sanitizeTenantId(tenantId);
        jdbcTemplate.execute("DROP SCHEMA IF EXISTS " + sanitizedTenantId + " CASCADE");
        System.out.println("Deleted tenant schema: " + sanitizedTenantId);
    }

    private String sanitizeTenantId(String tenantId) {
        // Only allow alphanumeric and underscore, must start with letter
        if (!tenantId.matches("^[a-zA-Z][a-zA-Z0-9_]*$")) {
            throw new IllegalArgumentException("Invalid tenant ID format");
        }
        return tenantId;
    }
}
