package com.rfbooks.dtos;

import java.util.List;

public class FinancialReportDto {
    private ReportSummary summary;
    private List<CategoryBreakdown> expensesByCategory;
    private List<CategoryBreakdown> incomeByCategory;
    private List<TrendData> trends;
    private List<VendorSpending> topVendors;
    private List<MonthlyComparison> monthlyComparison;
    private List<PaymentMethodBreakdown> paymentMethods;

    public static class ReportSummary {
        private double totalIncome;
        private double totalExpenses;
        private double netIncome;
        private double reconciliationRate;
        private String period;

        public ReportSummary() {}

        public ReportSummary(double totalIncome, double totalExpenses, double netIncome, double reconciliationRate, String period) {
            this.totalIncome = totalIncome;
            this.totalExpenses = totalExpenses;
            this.netIncome = netIncome;
            this.reconciliationRate = reconciliationRate;
            this.period = period;
        }

        public double getTotalIncome() { return totalIncome; }
        public void setTotalIncome(double totalIncome) { this.totalIncome = totalIncome; }

        public double getTotalExpenses() { return totalExpenses; }
        public void setTotalExpenses(double totalExpenses) { this.totalExpenses = totalExpenses; }

        public double getNetIncome() { return netIncome; }
        public void setNetIncome(double netIncome) { this.netIncome = netIncome; }

        public double getReconciliationRate() { return reconciliationRate; }
        public void setReconciliationRate(double reconciliationRate) { this.reconciliationRate = reconciliationRate; }

        public String getPeriod() { return period; }
        public void setPeriod(String period) { this.period = period; }
    }

    public static class CategoryBreakdown {
        private String category;
        private double amount;
        private double percentage;
        private int count;

        public CategoryBreakdown() {}

        public CategoryBreakdown(String category, double amount, double percentage, int count) {
            this.category = category;
            this.amount = amount;
            this.percentage = percentage;
            this.count = count;
        }

        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }

        public double getAmount() { return amount; }
        public void setAmount(double amount) { this.amount = amount; }

        public double getPercentage() { return percentage; }
        public void setPercentage(double percentage) { this.percentage = percentage; }

        public int getCount() { return count; }
        public void setCount(int count) { this.count = count; }
    }

    public static class TrendData {
        private String date;
        private double income;
        private double expenses;

        public TrendData() {}

        public TrendData(String date, double income, double expenses) {
            this.date = date;
            this.income = income;
            this.expenses = expenses;
        }

        public String getDate() { return date; }
        public void setDate(String date) { this.date = date; }

        public double getIncome() { return income; }
        public void setIncome(double income) { this.income = income; }

        public double getExpenses() { return expenses; }
        public void setExpenses(double expenses) { this.expenses = expenses; }
    }

    public static class VendorSpending {
        private String vendorName;
        private int transactionCount;
        private double totalAmount;

        public VendorSpending() {}

        public VendorSpending(String vendorName, int transactionCount, double totalAmount) {
            this.vendorName = vendorName;
            this.transactionCount = transactionCount;
            this.totalAmount = totalAmount;
        }

        public String getVendorName() { return vendorName; }
        public void setVendorName(String vendorName) { this.vendorName = vendorName; }

        public int getTransactionCount() { return transactionCount; }
        public void setTransactionCount(int transactionCount) { this.transactionCount = transactionCount; }

        public double getTotalAmount() { return totalAmount; }
        public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }
    }

    public static class MonthlyComparison {
        private String month;
        private double income;
        private double expenses;
        private double net;

        public MonthlyComparison() {}

        public MonthlyComparison(String month, double income, double expenses, double net) {
            this.month = month;
            this.income = income;
            this.expenses = expenses;
            this.net = net;
        }

        public String getMonth() { return month; }
        public void setMonth(String month) { this.month = month; }

        public double getIncome() { return income; }
        public void setIncome(double income) { this.income = income; }

        public double getExpenses() { return expenses; }
        public void setExpenses(double expenses) { this.expenses = expenses; }

        public double getNet() { return net; }
        public void setNet(double net) { this.net = net; }
    }

    public static class PaymentMethodBreakdown {
        private String method;
        private double amount;
        private double percentage;
        private int count;

        public PaymentMethodBreakdown() {}

        public PaymentMethodBreakdown(String method, double amount, double percentage, int count) {
            this.method = method;
            this.amount = amount;
            this.percentage = percentage;
            this.count = count;
        }

        public String getMethod() { return method; }
        public void setMethod(String method) { this.method = method; }

        public double getAmount() { return amount; }
        public void setAmount(double amount) { this.amount = amount; }

        public double getPercentage() { return percentage; }
        public void setPercentage(double percentage) { this.percentage = percentage; }

        public int getCount() { return count; }
        public void setCount(int count) { this.count = count; }
    }

    // Main class getters and setters
    public ReportSummary getSummary() { return summary; }
    public void setSummary(ReportSummary summary) { this.summary = summary; }

    public List<CategoryBreakdown> getExpensesByCategory() { return expensesByCategory; }
    public void setExpensesByCategory(List<CategoryBreakdown> expensesByCategory) { this.expensesByCategory = expensesByCategory; }

    public List<CategoryBreakdown> getIncomeByCategory() { return incomeByCategory; }
    public void setIncomeByCategory(List<CategoryBreakdown> incomeByCategory) { this.incomeByCategory = incomeByCategory; }

    public List<TrendData> getTrends() { return trends; }
    public void setTrends(List<TrendData> trends) { this.trends = trends; }

    public List<VendorSpending> getTopVendors() { return topVendors; }
    public void setTopVendors(List<VendorSpending> topVendors) { this.topVendors = topVendors; }

    public List<MonthlyComparison> getMonthlyComparison() { return monthlyComparison; }
    public void setMonthlyComparison(List<MonthlyComparison> monthlyComparison) { this.monthlyComparison = monthlyComparison; }

    public List<PaymentMethodBreakdown> getPaymentMethods() { return paymentMethods; }
    public void setPaymentMethods(List<PaymentMethodBreakdown> paymentMethods) { this.paymentMethods = paymentMethods; }
}
