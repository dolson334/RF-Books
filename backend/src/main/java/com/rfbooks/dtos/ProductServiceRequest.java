package com.rfbooks.dtos;

public class ProductServiceRequest {
    private String name;
    private String type;
    private Double defaultPrice;
    private String unitOfMeasure;
    private String description;
    private Long revenueAccountId;

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
}
