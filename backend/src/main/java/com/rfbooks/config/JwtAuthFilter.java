package com.rfbooks.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final SecretKey signingKey;

    public JwtAuthFilter(@Value("${rfbooks.jwt.secret:rfbooks-default-secret-key-change-in-production-min-32-chars}") String secret) {
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String token = extractToken(request);
            if (token != null) {
                Claims claims = Jwts.parser()
                        .verifyWith(signingKey)
                        .build()
                        .parseSignedClaims(token)
                        .getPayload();

                String resortAlias = claims.get("resortAlias", String.class);
                String userId = claims.getSubject();

                if (resortAlias != null && !resortAlias.isEmpty()) {
                    String sanitized = resortAlias.replaceAll("[^a-zA-Z0-9_]", "");
                    TenantContext.setCurrentTenant(sanitized);
                }
                if (userId != null && !userId.isEmpty()) {
                    AuthContext.setCurrentUserId(userId);
                }
            }
        } catch (Exception e) {
            // JWT is optional — if invalid or missing, continue with defaults
            // TenantInterceptor will set tenant from query param/header as fallback
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            AuthContext.clear();
        }
    }

    private String extractToken(HttpServletRequest request) {
        // Check Authorization header first
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        // Check URL parameter (for iframe initial load)
        String tokenParam = request.getParameter("token");
        if (tokenParam != null && !tokenParam.isEmpty()) {
            return tokenParam;
        }
        return null;
    }
}
