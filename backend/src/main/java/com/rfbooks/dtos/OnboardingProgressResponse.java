package com.rfbooks.dtos;

public class OnboardingProgressResponse {
    private boolean bankConnected;
    private boolean chartOfAccountsCreated;
    private boolean productsServicesCreated;
    private boolean taxesConfigured;
    private boolean completed;

    public OnboardingProgressResponse() {
    }

    public OnboardingProgressResponse(boolean bankConnected, boolean chartOfAccountsCreated, 
                                     boolean productsServicesCreated, boolean taxesConfigured, boolean completed) {
        this.bankConnected = bankConnected;
        this.chartOfAccountsCreated = chartOfAccountsCreated;
        this.productsServicesCreated = productsServicesCreated;
        this.taxesConfigured = taxesConfigured;
        this.completed = completed;
    }

    public boolean isBankConnected() {
        return bankConnected;
    }

    public void setBankConnected(boolean bankConnected) {
        this.bankConnected = bankConnected;
    }

    public boolean isChartOfAccountsCreated() {
        return chartOfAccountsCreated;
    }

    public void setChartOfAccountsCreated(boolean chartOfAccountsCreated) {
        this.chartOfAccountsCreated = chartOfAccountsCreated;
    }

    public boolean isProductsServicesCreated() {
        return productsServicesCreated;
    }

    public void setProductsServicesCreated(boolean productsServicesCreated) {
        this.productsServicesCreated = productsServicesCreated;
    }

    public boolean isTaxesConfigured() {
        return taxesConfigured;
    }

    public void setTaxesConfigured(boolean taxesConfigured) {
        this.taxesConfigured = taxesConfigured;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }
}
