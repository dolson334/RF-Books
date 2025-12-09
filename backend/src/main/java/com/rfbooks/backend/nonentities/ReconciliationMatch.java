package com.rfbooks.backend.nonentities;

public class ReconciliationMatch {
    private Long id;
    private String status; // MATCHED, MULTIPLE_MATCHES, UNMATCHED_PAYMENT, UNMATCHED_BANK_TRANSACTION
    private Double differenceAmount;
    private String reason;
    private Payment payment;
    private BankTransactionSummary bankTransaction;
    private String createdAt;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Double getDifferenceAmount() { return differenceAmount; }
    public void setDifferenceAmount(Double differenceAmount) { this.differenceAmount = differenceAmount; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public Payment getPayment() { return payment; }
    public void setPayment(Payment payment) { this.payment = payment; }

    public BankTransactionSummary getBankTransaction() { return bankTransaction; }
    public void setBankTransaction(BankTransactionSummary bankTransaction) { this.bankTransaction = bankTransaction; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}