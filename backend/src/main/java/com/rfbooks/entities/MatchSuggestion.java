package com.rfbooks.entities;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "match_suggestions")
public class MatchSuggestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "expense_id")
    private Long expenseId;

    @Column(name = "income_id")
    private Long incomeId;

    @Column(name = "transaction_id", nullable = false)
    private String transactionId;

    @Column(name = "confidence_score", nullable = false)
    private Integer confidenceScore;

    @Column(name = "match_reasons")
    private String matchReasons;

    @Column(name = "status", nullable = false)
    private String status = "PENDING";

    @Column(name = "created_at")
    private Instant createdAt;

    public MatchSuggestion() {
        this.createdAt = Instant.now();
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public Long getExpenseId() { return expenseId; }
    public void setExpenseId(Long expenseId) { this.expenseId = expenseId; }

    public Long getIncomeId() { return incomeId; }
    public void setIncomeId(Long incomeId) { this.incomeId = incomeId; }

    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }

    public Integer getConfidenceScore() { return confidenceScore; }
    public void setConfidenceScore(Integer confidenceScore) { this.confidenceScore = confidenceScore; }

    public String getMatchReasons() { return matchReasons; }
    public void setMatchReasons(String matchReasons) { this.matchReasons = matchReasons; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
