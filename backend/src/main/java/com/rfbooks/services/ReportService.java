package com.rfbooks.services;

import com.rfbooks.dtos.FinancialReportDto;
import com.rfbooks.dtos.ProfitLossReportDto;
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
    
    public ProfitLossReportDto generateProfitLossReport(LocalDate startDate, LocalDate endDate) {
        // Calculate date range if not provided
        if (startDate == null || endDate == null) {
            endDate = LocalDate.now();
            startDate = endDate.minusDays(30);
        }

        // Fetch data
        List<Expense> expenses = expenseRepository.findByUserIdAndDateRange(DEFAULT_USER_ID, startDate, endDate);
        Instant startInstant = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant endInstant = endDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant();
        List<PaymentEntity> payments = paymentRepository.findByUserIdAndDateRange(DEFAULT_USER_ID, startInstant, endInstant);

        ProfitLossReportDto report = new ProfitLossReportDto();
        List<ProfitLossReportDto.PLLineItem> lineItems = new ArrayList<>();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        // Calculate totals
        double totalIncome = payments.stream().mapToDouble(PaymentEntity::getAmount).sum();
        double totalExpenses = expenses.stream().mapToDouble(Expense::getAmount).sum();

        // INCOME SECTION
        ProfitLossReportDto.PLLineItem incomeCategory = new ProfitLossReportDto.PLLineItem("Income", totalIncome);
        incomeCategory.setIsCategory(true);
        incomeCategory.setExpanded(true);

        // Group income by category/type (based on guest_name or description)
        Map<String, List<PaymentEntity>> incomeByType = new HashMap<>();
        for (PaymentEntity payment : payments) {
            String type = categorizeIncome(payment);
            incomeByType.computeIfAbsent(type, k -> new ArrayList<>()).add(payment);
        }

        List<ProfitLossReportDto.PLLineItem> incomeChildren = new ArrayList<>();
        for (Map.Entry<String, List<PaymentEntity>> entry : incomeByType.entrySet()) {
            double amount = entry.getValue().stream().mapToDouble(PaymentEntity::getAmount).sum();
            double percentage = totalIncome > 0 ? (amount / totalIncome) * 100 : 0;

            ProfitLossReportDto.PLLineItem child = new ProfitLossReportDto.PLLineItem(entry.getKey(), amount);
            child.setPercentage(percentage);
            child.setShowTransactions(false);

            // Add transactions
            List<ProfitLossReportDto.Transaction> transactions = entry.getValue().stream()
                .map(p -> new ProfitLossReportDto.Transaction(
                    p.getPaymentDate().atZone(ZoneId.systemDefault()).toLocalDate().format(dateFormatter),
                    p.getGuestName() != null ? p.getGuestName() : "Payment",
                    p.getAmount(),
                    p.getReservationId()
                ))
                .collect(Collectors.toList());
            child.setTransactions(transactions);

            incomeChildren.add(child);
        }
        incomeCategory.setChildren(incomeChildren);
        lineItems.add(incomeCategory);

        // Total Income
        ProfitLossReportDto.PLLineItem totalIncomeItem = new ProfitLossReportDto.PLLineItem("Total Income", totalIncome);
        totalIncomeItem.setIsSubtotal(true);
        lineItems.add(totalIncomeItem);

        // EXPENSES SECTION - Group by category
        Map<String, List<Expense>> expensesByCategory = expenses.stream()
            .collect(Collectors.groupingBy(e -> e.getCategory() != null ? e.getCategory() : "Other Expenses"));

        // COGS categories
        List<String> cogsCategories = Arrays.asList("Food Costs", "Beverage Costs", "Activity Supplies");
        double totalCOGS = calculateCategoryTotal(expensesByCategory, cogsCategories);

        if (totalCOGS > 0) {
            ProfitLossReportDto.PLLineItem cogsCategory = new ProfitLossReportDto.PLLineItem("Cost of Goods Sold", totalCOGS);
            cogsCategory.setIsCategory(true);
            cogsCategory.setExpanded(true);
            cogsCategory.setChildren(buildExpenseChildren(expensesByCategory, cogsCategories, totalCOGS, dateFormatter));
            lineItems.add(cogsCategory);

            ProfitLossReportDto.PLLineItem totalCOGSItem = new ProfitLossReportDto.PLLineItem("Total COGS", totalCOGS);
            totalCOGSItem.setIsSubtotal(true);
            lineItems.add(totalCOGSItem);

            ProfitLossReportDto.PLLineItem grossProfit = new ProfitLossReportDto.PLLineItem("Gross Profit", totalIncome - totalCOGS);
            grossProfit.setIsSubtotal(true);
            lineItems.add(grossProfit);
        }

        // Operating Expenses
        List<String> opexCategories = Arrays.asList("Payroll", "Benefits", "Payroll Taxes", "Marketing", 
            "Utilities", "Maintenance", "Insurance", "Office Supplies", "Professional Services", 
            "Bank Fees", "Software", "Miscellaneous");
        double totalOpex = calculateCategoryTotal(expensesByCategory, opexCategories);

        if (totalOpex > 0) {
            ProfitLossReportDto.PLLineItem opexCategory = new ProfitLossReportDto.PLLineItem("Operating Expenses", totalOpex);
            opexCategory.setIsCategory(true);
            opexCategory.setExpanded(true);
            opexCategory.setChildren(buildExpenseChildren(expensesByCategory, opexCategories, totalOpex, dateFormatter));
            lineItems.add(opexCategory);

            ProfitLossReportDto.PLLineItem totalOpexItem = new ProfitLossReportDto.PLLineItem("Total Operating Expenses", totalOpex);
            totalOpexItem.setIsSubtotal(true);
            lineItems.add(totalOpexItem);
        }

        // Net Income
        double netIncome = totalIncome - totalExpenses;
        ProfitLossReportDto.PLLineItem netIncomeItem = new ProfitLossReportDto.PLLineItem("Net Income", netIncome);
        netIncomeItem.setIsTotal(true);
        lineItems.add(netIncomeItem);

        report.setLineItems(lineItems);

        // Summary
        double profitMargin = totalIncome > 0 ? (netIncome / totalIncome) * 100 : 0;
        report.setSummary(new ProfitLossReportDto.ReportSummary(totalIncome, totalExpenses, netIncome, profitMargin));

        return report;
    }

    private String categorizeIncome(PaymentEntity payment) {
        String guestName = payment.getGuestName();
        String reservationId = payment.getReservationId();

        if (reservationId != null) {
            if (reservationId.startsWith("ROOM") || reservationId.startsWith("CABIN") || reservationId.startsWith("SUITE")) {
                return "Room Revenue";
            } else if (reservationId.startsWith("REST") || reservationId.startsWith("BAR") || 
                       reservationId.startsWith("EVENT") || reservationId.startsWith("RS")) {
                return "Food & Beverage";
            } else if (reservationId.startsWith("ACT") || reservationId.startsWith("SPA")) {
                return "Activities & Tours";
            }
        }

        if (guestName != null) {
            if (guestName.contains("Gift") || guestName.contains("Pet") || guestName.contains("Late")) {
                return "Other Income";
            }
        }

        return "Room Revenue"; // Default
    }

    private double calculateCategoryTotal(Map<String, List<Expense>> expensesByCategory, List<String> categories) {
        return categories.stream()
            .filter(expensesByCategory::containsKey)
            .mapToDouble(cat -> expensesByCategory.get(cat).stream().mapToDouble(Expense::getAmount).sum())
            .sum();
    }

    private List<ProfitLossReportDto.PLLineItem> buildExpenseChildren(
            Map<String, List<Expense>> expensesByCategory,
            List<String> categories,
            double total,
            DateTimeFormatter dateFormatter) {

        List<ProfitLossReportDto.PLLineItem> children = new ArrayList<>();

        for (String category : categories) {
            if (expensesByCategory.containsKey(category)) {
                List<Expense> categoryExpenses = expensesByCategory.get(category);
                double amount = categoryExpenses.stream().mapToDouble(Expense::getAmount).sum();
                double percentage = total > 0 ? (amount / total) * 100 : 0;

                ProfitLossReportDto.PLLineItem child = new ProfitLossReportDto.PLLineItem(category, amount);
                child.setPercentage(percentage);
                child.setShowTransactions(false);

                // Add transactions
                List<ProfitLossReportDto.Transaction> transactions = categoryExpenses.stream()
                    .map(e -> new ProfitLossReportDto.Transaction(
                        e.getExpenseDate().format(dateFormatter),
                        e.getNotes() != null ? e.getNotes() : category,
                        e.getAmount(),
                        e.getVendorName()
                    ))
                    .collect(Collectors.toList());
                child.setTransactions(transactions);

                children.add(child);
            }
        }

        return children;
    }
}
