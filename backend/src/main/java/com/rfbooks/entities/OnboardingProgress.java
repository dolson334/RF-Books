package com.rfbooks.entities;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "onboarding_progress")
public class OnboardingProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, unique = true)
    private String userId;

    @Column(name = "bank_connected")
    private Boolean bankConnected = false;

    @Column(name = "chart_of_accounts_created")
    private Boolean chartOfAccountsCreated = false;

    @Column(name = "products_services_created")
    private Boolean productsServicesCreated = false;

    @Column(name = "completed")
    private Boolean completed = false;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    public OnboardingProgress() {
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public Boolean getBankConnected() { return bankConnected; }
    public void setBankConnected(Boolean bankConnected) { this.bankConnected = bankConnected; }

    public Boolean getChartOfAccountsCreated() { return chartOfAccountsCreated; }
    public void setChartOfAccountsCreated(Boolean chartOfAccountsCreated) { this.chartOfAccountsCreated = chartOfAccountsCreated; }

    public Boolean getProductsServicesCreated() { return productsServicesCreated; }
    public void setProductsServicesCreated(Boolean productsServicesCreated) { this.productsServicesCreated = productsServicesCreated; }

    public Boolean getCompleted() { return completed; }
    public void setCompleted(Boolean completed) { this.completed = completed; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
