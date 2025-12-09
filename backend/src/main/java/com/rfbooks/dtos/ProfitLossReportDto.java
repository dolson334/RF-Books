package com.rfbooks.dtos;

import java.util.List;

public class ProfitLossReportDto {
    private List<PLLineItem> lineItems;
    private ReportSummary summary;

    public static class PLLineItem {
        private String name;
        private Double amount;
        private Double percentage;
        private Boolean isCategory;
        private Boolean isSubtotal;
        private Boolean isTotal;
        private List<PLLineItem> children;
        private Boolean expanded;
        private List<Transaction> transactions;
        private Boolean showTransactions;

        public PLLineItem() {}

        public PLLineItem(String name, Double amount) {
            this.name = name;
            this.amount = amount;
            this.expanded = false;
            this.showTransactions = false;
        }

        // Getters and setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public Double getAmount() { return amount; }
        public void setAmount(Double amount) { this.amount = amount; }

        public Double getPercentage() { return percentage; }
        public void setPercentage(Double percentage) { this.percentage = percentage; }

        public Boolean getIsCategory() { return isCategory; }
        public void setIsCategory(Boolean isCategory) { this.isCategory = isCategory; }

        public Boolean getIsSubtotal() { return isSubtotal; }
        public void setIsSubtotal(Boolean isSubtotal) { this.isSubtotal = isSubtotal; }

        public Boolean getIsTotal() { return isTotal; }
        public void setIsTotal(Boolean isTotal) { this.isTotal = isTotal; }

        public List<PLLineItem> getChildren() { return children; }
        public void setChildren(List<PLLineItem> children) { this.children = children; }

        public Boolean getExpanded() { return expanded; }
        public void setExpanded(Boolean expanded) { this.expanded = expanded; }

        public List<Transaction> getTransactions() { return transactions; }
        public void setTransactions(List<Transaction> transactions) { this.transactions = transactions; }

        public Boolean getShowTransactions() { return showTransactions; }
        public void setShowTransactions(Boolean showTransactions) { this.showTransactions = showTransactions; }
    }

    public static class Transaction {
        private String date;
        private String description;
        private Double amount;
        private String category;
        private String vendor;

        public Transaction() {}

        public Transaction(String date, String description, Double amount, String vendor) {
            this.date = date;
            this.description = description;
            this.amount = amount;
            this.vendor = vendor;
        }

        // Getters and setters
        public String getDate() { return date; }
        public void setDate(String date) { this.date = date; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public Double getAmount() { return amount; }
        public void setAmount(Double amount) { this.amount = amount; }

        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }

        public String getVendor() { return vendor; }
        public void setVendor(String vendor) { this.vendor = vendor; }
    }

    public static class ReportSummary {
        private Double totalRevenue;
        private Double totalExpenses;
        private Double netProfit;
        private Double profitMargin;

        public ReportSummary() {}

        public ReportSummary(Double totalRevenue, Double totalExpenses, Double netProfit, Double profitMargin) {
            this.totalRevenue = totalRevenue;
            this.totalExpenses = totalExpenses;
            this.netProfit = netProfit;
            this.profitMargin = profitMargin;
        }

        // Getters and setters
        public Double getTotalRevenue() { return totalRevenue; }
        public void setTotalRevenue(Double totalRevenue) { this.totalRevenue = totalRevenue; }

        public Double getTotalExpenses() { return totalExpenses; }
        public void setTotalExpenses(Double totalExpenses) { this.totalExpenses = totalExpenses; }

        public Double getNetProfit() { return netProfit; }
        public void setNetProfit(Double netProfit) { this.netProfit = netProfit; }

        public Double getProfitMargin() { return profitMargin; }
        public void setProfitMargin(Double profitMargin) { this.profitMargin = profitMargin; }
    }

    // Main class getters and setters
    public List<PLLineItem> getLineItems() { return lineItems; }
    public void setLineItems(List<PLLineItem> lineItems) { this.lineItems = lineItems; }

    public ReportSummary getSummary() { return summary; }
    public void setSummary(ReportSummary summary) { this.summary = summary; }
}
