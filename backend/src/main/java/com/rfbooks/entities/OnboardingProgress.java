package com.rfbooks.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "onboarding_progress")
public class OnboardingProgress {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false, unique = true)
    private String userId = "default-user";
    
    @Column(name = "bank_connected", nullable = false)
    private Boolean bankConnected = false;
    
    @Column(name = "chart_of_accounts_created", nullable = false)
    private Boolean chartOfAccountsCreated = false;
    
    @Column(name = "products_services_created", nullable = false)
    private Boolean productsServicesCreated = false;
    
    @Column(nullable = false)
    private Boolean completed = false;

    public OnboardingProgress() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Boolean getBankConnected() {
        return bankConnected;
    }

    public void setBankConnected(Boolean bankConnected) {
        this.bankConnected = bankConnected;
    }

    public Boolean getChartOfAccountsCreated() {
        return chartOfAccountsCreated;
    }

    public void setChartOfAccountsCreated(Boolean chartOfAccountsCreated) {
        this.chartOfAccountsCreated = chartOfAccountsCreated;
    }

    public Boolean getProductsServicesCreated() {
        return productsServicesCreated;
    }

    public void setProductsServicesCreated(Boolean productsServicesCreated) {
        this.productsServicesCreated = productsServicesCreated;
    }

    public Boolean getCompleted() {
        return completed;
    }

    public void setCompleted(Boolean completed) {
        this.completed = completed;
    }
}
