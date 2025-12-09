package com.rfbooks.services;

import com.rfbooks.dtos.FinancialReportDto;
import com.rfbooks.entities.Expense;
import com.rfbooks.entities.PaymentEntity;
import com.rfbooks.repos.ExpenseRepository;
import com.rfbooks.repos.PaymentRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReportService {

    private static final String DEFAULT_USER_ID = "default-user";
    private final ExpenseRepository expenseRepository;
    private final PaymentRepository paymentRepository;

    public ReportService(ExpenseRepository expenseRepository, PaymentRepository paymentRepository) {
        this.expenseRepository = expenseRepository;
        this.paymentRepository = paymentRepository;
    }

    public FinancialReportDto generateFinancialReport(String period, LocalDate startDate, LocalDate endDate) {
        // Calculate date range if not provided
        if (startDate == null || endDate == null) {
            LocalDate now = LocalDate.now();
            endDate = now;
            startDate = switch (period != null ? period : "month") {
                case "week" -> now.minusDays(7);
                case "quarter" -> now.minusDays(90);
                case "year" -> now.minusYears(1);
                default -> now.minusDays(30);
            };
        }

        // Fetch data
        List<Expense> expenses = expenseRepository.findByUserIdAndDateRange(DEFAULT_USER_ID, startDate, endDate);
        Instant startInstant = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant endInstant = endDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant();
        List<PaymentEntity> payments = paymentRepository.findByUserIdAndDateRange(DEFAULT_USER_ID, startInstant, endInstant);

        // Build report
        FinancialReportDto report = new FinancialReportDto();
        
        // Summary
        double totalExpenses = expenses.stream().mapToDouble(Expense::getAmount).sum();
        double totalIncome = payments.stream().mapToDouble(PaymentEntity::getAmount).sum();
        long reconciledCount = payments.stream().filter(p -> p.getReconciled() != null && p.getReconciled()).count();
        double reconciliationRate = payments.isEmpty() ? 0 : (reconciledCount * 100.0 / payments.size());
        
        report.setSummary(new FinancialReportDto.ReportSummary(
            totalIncome,
            totalExpenses,
            totalIncome - totalExpenses,
            reconciliationRate,
            period != null ? period : "month"
        ));

        // Expenses by category
        Map<String, List<Expense>> expensesByCategory = expenses.stream()
            .collect(Collectors.groupingBy(e -> e.getCategory() != null ? e.getCategory() : "Uncategorized"));
        
        List<FinancialReportDto.CategoryBreakdown> categoryBreakdowns = expensesByCategory.entrySet().stream()
            .map(entry -> {
                double amount = entry.getValue().stream().mapToDouble(Expense::getAmount).sum();
                double percentage = totalExpenses > 0 ? (amount / totalExpenses) * 100 : 0;
                return new FinancialReportDto.CategoryBreakdown(
                    entry.getKey(),
                    amount,
                    percentage,
                    entry.getValue().size()
                );
            })
            .sorted((a, b) -> Double.compare(b.getAmount(), a.getAmount()))
            .collect(Collectors.toList());
        
        report.setExpensesByCategory(categoryBreakdowns);

        // Income by category (for now, just one category)
        report.setIncomeByCategory(List.of(
            new FinancialReportDto.CategoryBreakdown("Guest Payments", totalIncome, 100.0, payments.size())
        ));

        // Trends (weekly aggregation)
        report.setTrends(generateTrends(expenses, payments, startDate, endDate));

        // Top vendors
        Map<String, List<Expense>> expensesByVendor = expenses.stream()
            .filter(e -> e.getVendorName() != null && !e.getVendorName().isEmpty())
            .collect(Collectors.groupingBy(Expense::getVendorName));
        
        List<FinancialReportDto.VendorSpending> topVendors = expensesByVendor.entrySet().stream()
            .map(entry -> {
                double amount = entry.getValue().stream().mapToDouble(Expense::getAmount).sum();
                return new FinancialReportDto.VendorSpending(
                    entry.getKey(),
                    entry.getValue().size(),
                    amount
                );
            })
            .sorted((a, b) -> Double.compare(b.getTotalAmount(), a.getTotalAmount()))
            .limit(10)
            .collect(Collectors.toList());
        
        report.setTopVendors(topVendors);

        // Monthly comparison
        report.setMonthlyComparison(generateMonthlyComparison(expenses, payments, startDate, endDate));

        // Payment methods
        Map<String, List<PaymentEntity>> paymentsByMethod = payments.stream()
            .collect(Collectors.groupingBy(p -> p.getMethod() != null ? p.getMethod() : "Unknown"));
        
        List<FinancialReportDto.PaymentMethodBreakdown> paymentMethods = paymentsByMethod.entrySet().stream()
            .map(entry -> {
                double amount = entry.getValue().stream().mapToDouble(PaymentEntity::getAmount).sum();
                double percentage = totalIncome > 0 ? (amount / totalIncome) * 100 : 0;
                return new FinancialReportDto.PaymentMethodBreakdown(
                    entry.getKey(),
                    amount,
                    percentage,
                    entry.getValue().size()
                );
            })
            .sorted((a, b) -> Double.compare(b.getAmount(), a.getAmount()))
            .collect(Collectors.toList());
        
        report.setPaymentMethods(paymentMethods);

        return report;
    }

    private List<FinancialReportDto.TrendData> generateTrends(List<Expense> expenses, List<PaymentEntity> payments, LocalDate startDate, LocalDate endDate) {
        Map<String, Double> expensesByDate = new TreeMap<>();
        Map<String, Double> incomeByDate = new TreeMap<>();
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd");
        
        // Aggregate expenses by date
        expenses.forEach(expense -> {
            String dateKey = expense.getExpenseDate().format(formatter);
            expensesByDate.merge(dateKey, expense.getAmount(), Double::sum);
        });
        
        // Aggregate payments by date
        payments.forEach(payment -> {
            LocalDate date = payment.getPaymentDate().atZone(ZoneId.systemDefault()).toLocalDate();
            String dateKey = date.format(formatter);
            incomeByDate.merge(dateKey, payment.getAmount(), Double::sum);
        });
        
        // Combine into trend data (limit to 10 points)
        Set<String> allDates = new TreeSet<>();
        allDates.addAll(expensesByDate.keySet());
        allDates.addAll(incomeByDate.keySet());
        
        return allDates.stream()
            .limit(10)
            .map(date -> new FinancialReportDto.TrendData(
                date,
                incomeByDate.getOrDefault(date, 0.0),
                expensesByDate.getOrDefault(date, 0.0)
            ))
            .collect(Collectors.toList());
    }

    private List<FinancialReportDto.MonthlyComparison> generateMonthlyComparison(List<Expense> expenses, List<PaymentEntity> payments, LocalDate startDate, LocalDate endDate) {
        Map<String, Double> expensesByMonth = new TreeMap<>();
        Map<String, Double> incomeByMonth = new TreeMap<>();
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM yyyy");
        
        // Aggregate expenses by month
        expenses.forEach(expense -> {
            String monthKey = expense.getExpenseDate().format(formatter);
            expensesByMonth.merge(monthKey, expense.getAmount(), Double::sum);
        });
        
        // Aggregate payments by month
        payments.forEach(payment -> {
            LocalDate date = payment.getPaymentDate().atZone(ZoneId.systemDefault()).toLocalDate();
            String monthKey = date.format(formatter);
            incomeByMonth.merge(monthKey, payment.getAmount(), Double::sum);
        });
        
        // Combine into monthly comparison
        Set<String> allMonths = new TreeSet<>();
        allMonths.addAll(expensesByMonth.keySet());
        allMonths.addAll(incomeByMonth.keySet());
        
        return allMonths.stream()
            .map(month -> {
                double income = incomeByMonth.getOrDefault(month, 0.0);
                double expense = expensesByMonth.getOrDefault(month, 0.0);
                return new FinancialReportDto.MonthlyComparison(month, income, expense, income - expense);
            })
            .collect(Collectors.toList());
    }
}
