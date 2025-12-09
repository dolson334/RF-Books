package com.rfbooks.dtos;

import java.time.Instant;
import java.time.LocalDate;

public class ReconciliationSummary {
    private Long id;
    private Instant runAt;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer matchedCount;
    private Integer unmatchedPaymentCount;
    private Integer unmatchedBankCount;
    private Integer totalPayments;
    private Integer totalBankTransactions;
    private String status;
    private String errorMessage;
    private Boolean hasIssues;

    public ReconciliationSummary() {}

    public ReconciliationSummary(Long id, Instant runAt, LocalDate startDate, LocalDate endDate,
                                  Integer matchedCount, Integer unmatchedPaymentCount, 
                                  Integer unmatchedBankCount, Integer totalPayments,
                                  Integer totalBankTransactions, String status, String errorMessage) {
        this.id = id;
        this.runAt = runAt;
        this.startDate = startDate;
        this.endDate = endDate;
        this.matchedCount = matchedCount;
        this.unmatchedPaymentCount = unmatchedPaymentCount;
        this.unmatchedBankCount = unmatchedBankCount;
        this.totalPayments = totalPayments;
        this.totalBankTransactions = totalBankTransactions;
        this.status = status;
        this.errorMessage = errorMessage;
        this.hasIssues = unmatchedPaymentCount > 0 || unmatchedBankCount > 0 || "FAILED".equals(status);
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Boolean getHasIssues() {
        return hasIssues;
    }

    public void setHasIssues(Boolean hasIssues) {
        this.hasIssues = hasIssues;
    }
}
