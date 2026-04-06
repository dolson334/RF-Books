package com.rfbooks.dtos;

import java.math.BigDecimal;

public class CategoryBreakdown {
    private String category;
    private BigDecimal total;
    private int count;
    private double percentage;

    public CategoryBreakdown() {}

    public CategoryBreakdown(String category, BigDecimal total, Long count) {
        this.category = category != null ? category : "Uncategorized";
        this.total = total;
        this.count = count != null ? count.intValue() : 0;
    }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }

    public int getCount() { return count; }
    public void setCount(int count) { this.count = count; }

    public double getPercentage() { return percentage; }
    public void setPercentage(double percentage) { this.percentage = percentage; }
}
