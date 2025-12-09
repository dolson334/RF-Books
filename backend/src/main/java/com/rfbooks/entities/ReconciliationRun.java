package com.rfbooks.entities;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "reconciliation_runs")
public class ReconciliationRun {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "run_at", nullable = false)
    private Instant runAt;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "matched_count", nullable = false)
    private Integer matchedCount = 0;

    @Column(name = "unmatched_payment_count", nullable = false)
    private Integer unmatchedPaymentCount = 0;

    @Column(name = "unmatched_bank_count", nullable = false)
    private Integer unmatchedBankCount = 0;

    @Column(name = "total_payments", nullable = false)
    private Integer totalPayments = 0;

    @Column(name = "total_bank_transactions", nullable = false)
    private Integer totalBankTransactions = 0;

    @Column(nullable = false)
    private String status = "COMPLETED"; // COMPLETED, FAILED, NO_BANK_CONNECTION

    @Column(name = "error_message", length = 1000)
    private String errorMessage;

    @Column(name = "results_json", columnDefinition = "TEXT")
    private String resultsJson; // Store full reconciliation results as JSON

    @Column(name = "created_at")
    private Instant createdAt;

    // Getters and setters
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

    public Instant getRunAt() {
        return runAt;
    }

    public void setRunAt(Instant runAt) {
        this.runAt = runAt;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public Integer getMatchedCount() {
        return matchedCount;
    }

    public void setMatchedCount(Integer matchedCount) {
        this.matchedCount = matchedCount;
    }

    public Integer getUnmatchedPaymentCount() {
        return unmatchedPaymentCount;
    }

    public void setUnmatchedPaymentCount(Integer unmatchedPaymentCount) {
        this.unmatchedPaymentCount = unmatchedPaymentCount;
    }

    public Integer getUnmatchedBankCount() {
        return unmatchedBankCount;
    }

    public void setUnmatchedBankCount(Integer unmatchedBankCount) {
        this.unmatchedBankCount = unmatchedBankCount;
    }

    public Integer getTotalPayments() {
        return totalPayments;
    }

    public void setTotalPayments(Integer totalPayments) {
        this.totalPayments = totalPayments;
    }

    public Integer getTotalBankTransactions() {
        return totalBankTransactions;
    }

    public void setTotalBankTransactions(Integer totalBankTransactions) {
        this.totalBankTransactions = totalBankTransactions;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getResultsJson() {
        return resultsJson;
    }

    public void setResultsJson(String resultsJson) {
        this.resultsJson = resultsJson;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
