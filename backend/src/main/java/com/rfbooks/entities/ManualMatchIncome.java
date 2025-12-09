package com.rfbooks.entities;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "manual_match_income")
public class ManualMatchIncome {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "income_id", nullable = false)
    private Long incomeId;

    @Column(name = "transaction_id", nullable = false)
    private String transactionId;

    @Column(name = "matched_at", nullable = false)
    private Instant matchedAt;

    @Column(name = "matched_by")
    private String matchedBy;

    @Column(name = "created_at")
    private Instant createdAt;

    public ManualMatchIncome() {
        this.matchedAt = Instant.now();
        this.createdAt = Instant.now();
    }

    public ManualMatchIncome(String userId, Long incomeId, String transactionId) {
        this();
        this.userId = userId;
        this.incomeId = incomeId;
        this.transactionId = transactionId;
    }

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

    public Long getIncomeId() {
        return incomeId;
    }

    public void setIncomeId(Long incomeId) {
        this.incomeId = incomeId;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public Instant getMatchedAt() {
        return matchedAt;
    }

    public void setMatchedAt(Instant matchedAt) {
        this.matchedAt = matchedAt;
    }

    public String getMatchedBy() {
        return matchedBy;
    }

    public void setMatchedBy(String matchedBy) {
        this.matchedBy = matchedBy;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
