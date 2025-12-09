package com.rfbooks.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class TenantInterceptor implements HandlerInterceptor {

    private static final String RESORT_ALIAS_PARAM = "resortAlias";
    private static final String DEFAULT_TENANT = "testresort";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // Try to get tenant from query parameter first
        String tenantId = request.getParameter(RESORT_ALIAS_PARAM);
        
        // Fallback to header if param not present
        if (tenantId == null || tenantId.isEmpty()) {
            tenantId = request.getHeader("X-Tenant-ID");
        }
        
        // Use default if still not found
        if (tenantId == null || tenantId.isEmpty()) {
            tenantId = DEFAULT_TENANT;
        }
        
        // Ensure tenant ID is safe (prevent SQL injection)
        tenantId = sanitizeTenantId(tenantId);
        
        TenantContext.setCurrentTenant(tenantId);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, 
                                Object handler, Exception ex) {
        TenantContext.clear();
    }

    private String sanitizeTenantId(String tenantId) {
        // Only allow alphanumeric and underscore
        return tenantId.replaceAll("[^a-zA-Z0-9_]", "");
    }
}
