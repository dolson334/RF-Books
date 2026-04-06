package com.rfbooks.services;

import com.rfbooks.config.AuthContext;
import com.rfbooks.dtos.CategoryBreakdown;
import com.rfbooks.dtos.FinancialSummary;
import com.rfbooks.dtos.MonthlyTrend;
import com.rfbooks.repos.ExpenseRepository;
import com.rfbooks.repos.IncomeRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

@Service
public class ReportService {

    private final IncomeRepository incomeRepository;
    private final ExpenseRepository expenseRepository;

    public ReportService(IncomeRepository incomeRepository, ExpenseRepository expenseRepository) {
        this.incomeRepository = incomeRepository;
        this.expenseRepository = expenseRepository;
    }

    public FinancialSummary getSummary(LocalDate startDate, LocalDate endDate, String periodLabel) {
        String userId = AuthContext.getCurrentUserId();

        BigDecimal totalIncome = incomeRepository.sumAmountByUserIdAndDateRange(userId, startDate, endDate);
        BigDecimal totalExpenses = expenseRepository.sumAmountByUserIdAndDateRange(userId, startDate, endDate);
        long incomeCount = incomeRepository.countByUserIdAndDateRange(userId, startDate, endDate);
        long expenseCount = expenseRepository.countByUserIdAndDateRange(userId, startDate, endDate);
        long reconciledIncome = incomeRepository.countReconciledByUserIdAndDateRange(userId, startDate, endDate);
        long reconciledExpenses = expenseRepository.countReconciledByUserIdAndDateRange(userId, startDate, endDate);

        long totalCount = incomeCount + expenseCount;
        long reconciledCount = reconciledIncome + reconciledExpenses;
        double reconciliationRate = totalCount > 0
                ? (double) reconciledCount / totalCount * 100.0
                : 0.0;

        FinancialSummary summary = new FinancialSummary();
        summary.setTotalIncome(totalIncome);
        summary.setTotalExpenses(totalExpenses);
        summary.setNetIncome(totalIncome.subtract(totalExpenses));
        summary.setIncomeCount((int) incomeCount);
        summary.setExpenseCount((int) expenseCount);
        summary.setReconciledCount((int) reconciledCount);
        summary.setUnreconciledCount((int) (totalCount - reconciledCount));
        summary.setReconciliationRate(BigDecimal.valueOf(reconciliationRate)
                .setScale(1, RoundingMode.HALF_UP).doubleValue());
        summary.setPeriod(periodLabel);
        summary.setStartDate(startDate.toString());
        summary.setEndDate(endDate.toString());

        return summary;
    }

    public List<CategoryBreakdown> getIncomeByCategory(LocalDate startDate, LocalDate endDate) {
        String userId = AuthContext.getCurrentUserId();
        List<Object[]> rows = incomeRepository.sumByCategory(userId, startDate, endDate);
        return computePercentages(toBreakdowns(rows));
    }

    public List<CategoryBreakdown> getExpensesByCategory(LocalDate startDate, LocalDate endDate) {
        String userId = AuthContext.getCurrentUserId();
        List<Object[]> rows = expenseRepository.sumByCategory(userId, startDate, endDate);
        return computePercentages(toBreakdowns(rows));
    }

    public List<MonthlyTrend> getMonthlyTrend(int months) {
        String userId = AuthContext.getCurrentUserId();
        YearMonth current = YearMonth.now();
        List<MonthlyTrend> trends = new ArrayList<>();

        for (int i = months - 1; i >= 0; i--) {
            YearMonth ym = current.minusMonths(i);
            LocalDate start = ym.atDay(1);
            LocalDate end = ym.atEndOfMonth();

            BigDecimal income = incomeRepository.sumAmountByUserIdAndDateRange(userId, start, end);
            BigDecimal expenses = expenseRepository.sumAmountByUserIdAndDateRange(userId, start, end);

            trends.add(new MonthlyTrend(ym.toString(), income, expenses));
        }

        return trends;
    }

    private List<CategoryBreakdown> toBreakdowns(List<Object[]> rows) {
        List<CategoryBreakdown> result = new ArrayList<>();
        for (Object[] row : rows) {
            String category = row[0] != null ? row[0].toString() : null;
            BigDecimal total = row[1] instanceof BigDecimal ? (BigDecimal) row[1] : new BigDecimal(row[1].toString());
            long count = row[2] instanceof Number ? ((Number) row[2]).longValue() : 0L;
            result.add(new CategoryBreakdown(category, total, count));
        }
        return result;
    }

    private List<CategoryBreakdown> computePercentages(List<CategoryBreakdown> breakdowns) {
        BigDecimal total = breakdowns.stream()
                .map(CategoryBreakdown::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (total.compareTo(BigDecimal.ZERO) > 0) {
            for (CategoryBreakdown b : breakdowns) {
                double pct = b.getTotal().divide(total, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100))
                        .setScale(1, RoundingMode.HALF_UP)
                        .doubleValue();
                b.setPercentage(pct);
            }
        }
        return breakdowns;
    }
}
