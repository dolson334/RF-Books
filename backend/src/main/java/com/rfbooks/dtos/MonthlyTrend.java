package com.rfbooks.dtos;

import java.math.BigDecimal;

public class MonthlyTrend {
    private String month;
    private BigDecimal income;
    private BigDecimal expenses;
    private BigDecimal net;

    public MonthlyTrend() {}

    public MonthlyTrend(String month, BigDecimal income, BigDecimal expenses) {
        this.month = month;
        this.income = income != null ? income : BigDecimal.ZERO;
        this.expenses = expenses != null ? expenses : BigDecimal.ZERO;
        this.net = this.income.subtract(this.expenses);
    }

    public String getMonth() { return month; }
    public void setMonth(String month) { this.month = month; }

    public BigDecimal getIncome() { return income; }
    public void setIncome(BigDecimal income) { this.income = income; }

    public BigDecimal getExpenses() { return expenses; }
    public void setExpenses(BigDecimal expenses) { this.expenses = expenses; }

    public BigDecimal getNet() { return net; }
    public void setNet(BigDecimal net) { this.net = net; }
}
