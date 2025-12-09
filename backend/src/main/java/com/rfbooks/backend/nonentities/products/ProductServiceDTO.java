// ProductServiceDTO.java
package com.rfbooks.backend.nonentities.products;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.rfbooks.backend.entities.ProductService;

public class ProductServiceDTO {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("name")
    private String name;

    @JsonProperty("type")
    private ItemType type;

    @JsonProperty("defaultPrice")
    private Double defaultPrice;

    @JsonProperty("unitOfMeasure")
    private String unitOfMeasure;

    @JsonProperty("description")
    private String description;

    @JsonProperty("revenueAccountId")
    private Long revenueAccountId;

    // Constructors
    public ProductServiceDTO() {}

    public ProductServiceDTO(ProductService entity) {
        this.id = entity.getId();
        this.name = entity.getName();
        this.type = entity.getType();
        this.defaultPrice = entity.getDefaultPrice();
        this.unitOfMeasure = entity.getUnitOfMeasure();
        this.description = entity.getDescription();
        this.revenueAccountId = entity.getRevenueAccountId();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ItemType getType() {
        return type;
    }

    public void setType(ItemType type) {
        this.type = type;
    }

    public Double getDefaultPrice() {
        return defaultPrice;
    }

    public void setDefaultPrice(Double defaultPrice) {
        this.defaultPrice = defaultPrice;
    }

    public String getUnitOfMeasure() {
        return unitOfMeasure;
    }

    public void setUnitOfMeasure(String unitOfMeasure) {
        this.unitOfMeasure = unitOfMeasure;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getRevenueAccountId() {
        return revenueAccountId;
    }

    public void setRevenueAccountId(Long revenueAccountId) {
        this.revenueAccountId = revenueAccountId;
    }

    // Convert DTO to Entity
    public ProductService toEntity() {
        ProductService entity = new ProductService();
        entity.setName(this.name);
        entity.setType(this.type);
        entity.setDefaultPrice(this.defaultPrice);
        entity.setUnitOfMeasure(this.unitOfMeasure);
        entity.setDescription(this.description);
        entity.setRevenueAccountId(this.revenueAccountId);
        return entity;
    }
}