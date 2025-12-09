package com.rfbooks.controllers;

import com.rfbooks.services.TenantService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/tenants")
public class TenantController {

    private final TenantService tenantService;

    public TenantController(TenantService tenantService) {
        this.tenantService = tenantService;
    }

    @PostMapping("/create")
    public ResponseEntity<Map<String, String>> createTenant(@RequestBody Map<String, String> request) {
        String tenantId = request.get("tenantId");
        
        if (tenantId == null || tenantId.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Tenant ID is required"));
        }

        try {
            if (tenantService.tenantExists(tenantId)) {
                return ResponseEntity.badRequest().body(Map.of("error", "Tenant already exists"));
            }

            tenantService.createTenantSchema(tenantId);
            return ResponseEntity.ok(Map.of("message", "Tenant created successfully", "tenantId", tenantId));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to create tenant: " + e.getMessage()));
        }
    }

    @GetMapping("/exists/{tenantId}")
    public ResponseEntity<Map<String, Boolean>> checkTenantExists(@PathVariable String tenantId) {
        boolean exists = tenantService.tenantExists(tenantId);
        return ResponseEntity.ok(Map.of("exists", exists));
    }
}
