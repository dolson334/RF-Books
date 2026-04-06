package com.rfbooks.entities;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "products_services")
public class ProductService {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "type", nullable = false)
    private String type;

    @Column(name = "default_price")
    private Double defaultPrice;

    @Column(name = "unit_of_measure")
    private String unitOfMeasure;

    @Column(name = "description")
    private String description;

    @Column(name = "revenue_account_id")
    private Long revenueAccountId;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    public ProductService() {
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Double getDefaultPrice() { return defaultPrice; }
    public void setDefaultPrice(Double defaultPrice) { this.defaultPrice = defaultPrice; }

    public String getUnitOfMeasure() { return unitOfMeasure; }
    public void setUnitOfMeasure(String unitOfMeasure) { this.unitOfMeasure = unitOfMeasure; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Long getRevenueAccountId() { return revenueAccountId; }
    public void setRevenueAccountId(Long revenueAccountId) { this.revenueAccountId = revenueAccountId; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
