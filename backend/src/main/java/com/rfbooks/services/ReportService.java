package com.rfbooks.services;

import com.rfbooks.dtos.FinancialReportDto;
import com.rfbooks.dtos.ProfitLossReportDto;
import com.rfbooks.entities.Expense;
import com.rfbooks.entities.Income;
import com.rfbooks.repos.ExpenseRepository;
import com.rfbooks.repos.IncomeRepository;
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
    private final IncomeRepository incomeRepository;

    public ReportService(ExpenseRepository expenseRepository, IncomeRepository incomeRepository) {
        this.expenseRepository = expenseRepository;
        this.incomeRepository = incomeRepository;
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
        List<Income> income = incomeRepository.findByUserIdAndDateRange(DEFAULT_USER_ID, startDate, endDate);

        // Build report
        FinancialReportDto report = new FinancialReportDto();
        
        // Summary
        double totalExpenses = expenses.stream().mapToDouble(Expense::getAmount).sum();
        double totalIncome = income.stream().mapToDouble(Income::getAmount).sum();
        long reconciledCount = income.stream().filter(i -> i.getReconciled() != null && i.getReconciled()).count();
        double reconciliationRate = income.isEmpty() ? 0 : (reconciledCount * 100.0 / income.size());
        
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

        // Income by category
        Map<String, List<Income>> incomeByCategory = income.stream()
            .collect(Collectors.groupingBy(i -> i.getCategory() != null ? i.getCategory() : "Other Revenue"));
        
        List<FinancialReportDto.CategoryBreakdown> incomeBreakdowns = incomeByCategory.entrySet().stream()
            .map(entry -> {
                double amount = entry.getValue().stream().mapToDouble(Income::getAmount).sum();
                double percentage = totalIncome > 0 ? (amount / totalIncome) * 100 : 0;
                return new FinancialReportDto.CategoryBreakdown(
                    entry.getKey(),
                    amount,
                    percentage,
                    entry.getValue().size()
                );
            })
            .sorted((a, b) -> Double.compare(b.getAmount(), a.getAmount()))
            .collect(Collectors.toList());
        
        report.setIncomeByCategory(incomeBreakdowns);

        // Trends (weekly aggregation)
        report.setTrends(generateTrends(expenses, income, startDate, endDate));

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
        report.setMonthlyComparison(generateMonthlyComparison(expenses, income, startDate, endDate));

        // Payment methods
        Map<String, List<Income>> incomeByMethod = income.stream()
            .collect(Collectors.groupingBy(i -> i.getPaymentMethod() != null ? i.getPaymentMethod() : "Unknown"));
        
        List<FinancialReportDto.PaymentMethodBreakdown> paymentMethods = incomeByMethod.entrySet().stream()
            .map(entry -> {
                double amount = entry.getValue().stream().mapToDouble(Income::getAmount).sum();
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

    private List<FinancialReportDto.TrendData> generateTrends(List<Expense> expenses, List<Income> income, LocalDate startDate, LocalDate endDate) {
        Map<String, Double> expensesByDate = new TreeMap<>();
        Map<String, Double> incomeByDate = new TreeMap<>();
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd");
        
        // Aggregate expenses by date
        expenses.forEach(expense -> {
            String dateKey = expense.getExpenseDate().format(formatter);
            expensesByDate.merge(dateKey, expense.getAmount(), Double::sum);
        });
        
        // Aggregate income by date
        income.forEach(inc -> {
            String dateKey = inc.getIncomeDate().format(formatter);
            incomeByDate.merge(dateKey, inc.getAmount(), Double::sum);
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

    private List<FinancialReportDto.MonthlyComparison> generateMonthlyComparison(List<Expense> expenses, List<Income> income, LocalDate startDate, LocalDate endDate) {
        Map<String, Double> expensesByMonth = new TreeMap<>();
        Map<String, Double> incomeByMonth = new TreeMap<>();
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM yyyy");
        
        // Aggregate expenses by month
        expenses.forEach(expense -> {
            String monthKey = expense.getExpenseDate().format(formatter);
            expensesByMonth.merge(monthKey, expense.getAmount(), Double::sum);
        });
        
        // Aggregate income by month
        income.forEach(inc -> {
            String monthKey = inc.getIncomeDate().format(formatter);
            incomeByMonth.merge(monthKey, inc.getAmount(), Double::sum);
        });
        
        // Combine into monthly comparison
        Set<String> allMonths = new TreeSet<>();
        allMonths.addAll(expensesByMonth.keySet());
        allMonths.addAll(incomeByMonth.keySet());
        
        return allMonths.stream()
            .map(month -> {
                double incomeAmount = incomeByMonth.getOrDefault(month, 0.0);
                double expenseAmount = expensesByMonth.getOrDefault(month, 0.0);
                return new FinancialReportDto.MonthlyComparison(month, incomeAmount, expenseAmount, incomeAmount - expenseAmount);
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
        List<Income> income = incomeRepository.findByUserIdAndDateRange(DEFAULT_USER_ID, startDate, endDate);

        ProfitLossReportDto report = new ProfitLossReportDto();
        List<ProfitLossReportDto.PLLineItem> lineItems = new ArrayList<>();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        // Calculate totals
        double totalIncome = income.stream().mapToDouble(Income::getAmount).sum();
        double totalExpenses = expenses.stream().mapToDouble(Expense::getAmount).sum();

        // INCOME SECTION
        ProfitLossReportDto.PLLineItem incomeCategory = new ProfitLossReportDto.PLLineItem("Income", totalIncome);
        incomeCategory.setIsCategory(true);
        incomeCategory.setExpanded(true);

        // Group income by category
        Map<String, List<Income>> incomeByType = income.stream()
            .collect(Collectors.groupingBy(i -> i.getCategory() != null ? i.getCategory() : "Other Revenue"));

        List<ProfitLossReportDto.PLLineItem> incomeChildren = new ArrayList<>();
        for (Map.Entry<String, List<Income>> entry : incomeByType.entrySet()) {
            double amount = entry.getValue().stream().mapToDouble(Income::getAmount).sum();
            double percentage = totalIncome > 0 ? (amount / totalIncome) * 100 : 0;

            ProfitLossReportDto.PLLineItem child = new ProfitLossReportDto.PLLineItem(entry.getKey(), amount);
            child.setPercentage(percentage);
            child.setShowTransactions(false);

            // Add transactions
            List<ProfitLossReportDto.Transaction> transactions = entry.getValue().stream()
                .map(i -> new ProfitLossReportDto.Transaction(
                    i.getIncomeDate().format(dateFormatter),
                    i.getSource() != null ? i.getSource() : "Income",
                    i.getAmount(),
                    i.getReferenceNumber()
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
