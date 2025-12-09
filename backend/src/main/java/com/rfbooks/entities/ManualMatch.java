package com.rfbooks.entities;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "manual_matches")
public class ManualMatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "payment_id", nullable = false)
    private String paymentId;

    @Column(name = "transaction_id", nullable = false)
    private String transactionId;

    @Column(name = "matched_at", nullable = false)
    private Instant matchedAt;

    @Column(name = "matched_by")
    private String matchedBy;

    @Column(name = "created_at")
    private Instant createdAt;

    public ManualMatch() {
        this.matchedAt = Instant.now();
        this.createdAt = Instant.now();
    }

    public ManualMatch(String userId, String paymentId, String transactionId) {
        this();
        this.userId = userId;
        this.paymentId = paymentId;
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

    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
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
