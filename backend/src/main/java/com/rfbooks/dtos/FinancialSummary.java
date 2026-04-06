package com.rfbooks.dtos;

import java.math.BigDecimal;

public class FinancialSummary {
    private BigDecimal totalIncome;
    private BigDecimal totalExpenses;
    private BigDecimal netIncome;
    private int incomeCount;
    private int expenseCount;
    private int reconciledCount;
    private int unreconciledCount;
    private double reconciliationRate;
    private String period;
    private String startDate;
    private String endDate;

    public FinancialSummary() {}

    public BigDecimal getTotalIncome() { return totalIncome; }
    public void setTotalIncome(BigDecimal totalIncome) { this.totalIncome = totalIncome; }

    public BigDecimal getTotalExpenses() { return totalExpenses; }
    public void setTotalExpenses(BigDecimal totalExpenses) { this.totalExpenses = totalExpenses; }

    public BigDecimal getNetIncome() { return netIncome; }
    public void setNetIncome(BigDecimal netIncome) { this.netIncome = netIncome; }

    public int getIncomeCount() { return incomeCount; }
    public void setIncomeCount(int incomeCount) { this.incomeCount = incomeCount; }

    public int getExpenseCount() { return expenseCount; }
    public void setExpenseCount(int expenseCount) { this.expenseCount = expenseCount; }

    public int getReconciledCount() { return reconciledCount; }
    public void setReconciledCount(int reconciledCount) { this.reconciledCount = reconciledCount; }

    public int getUnreconciledCount() { return unreconciledCount; }
    public void setUnreconciledCount(int unreconciledCount) { this.unreconciledCount = unreconciledCount; }

    public double getReconciliationRate() { return reconciliationRate; }
    public void setReconciliationRate(double reconciliationRate) { this.reconciliationRate = reconciliationRate; }

    public String getPeriod() { return period; }
    public void setPeriod(String period) { this.period = period; }

    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }

    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }
}
