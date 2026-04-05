package com.rfbooks.services;

import com.rfbooks.config.AuthContext;
import com.rfbooks.dtos.ReconciliationSummary;
import com.rfbooks.entities.ManualMatchExpense;
import com.rfbooks.entities.ManualMatchIncome;
import com.rfbooks.entities.ReconciliationRun;
import com.rfbooks.nonentities.PlaidTransaction;
import com.rfbooks.repos.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class ReconciliationService {
    
    private final PlaidService plaidService;
    private final ReconciliationRunRepository runRepository;
    private final ManualMatchExpenseRepository manualMatchExpenseRepository;
    private final ManualMatchIncomeRepository manualMatchIncomeRepository;
    private final ExpenseRepository expenseRepository;
    private final IncomeRepository incomeRepository;
    private final ObjectMapper objectMapper;

    public ReconciliationService(PlaidService plaidService, 
                                  ReconciliationRunRepository runRepository,
                                  ManualMatchExpenseRepository manualMatchExpenseRepository,
                                  ManualMatchIncomeRepository manualMatchIncomeRepository,
                                  ExpenseRepository expenseRepository,
                                  IncomeRepository incomeRepository,
                                  ObjectMapper objectMapper) {
        this.plaidService = plaidService;
        this.runRepository = runRepository;
        this.manualMatchExpenseRepository = manualMatchExpenseRepository;
        this.manualMatchIncomeRepository = manualMatchIncomeRepository;
        this.expenseRepository = expenseRepository;
        this.incomeRepository = incomeRepository;
        this.objectMapper = objectMapper;
    }

    // Expense reconciliation methods
    public ManualMatchExpense createManualExpenseMatch(Long expenseId, String transactionId) {
        ManualMatchExpense match = new ManualMatchExpense(AuthContext.getCurrentUserId(), expenseId, transactionId);
        
        // Mark the expense as reconciled
        expenseRepository.findById(expenseId).ifPresent(expense -> {
            expense.setReconciled(true);
            expenseRepository.save(expense);
        });
        
        return manualMatchExpenseRepository.save(match);
    }

    public void deleteManualExpenseMatch(Long expenseId) {
        manualMatchExpenseRepository.findByUserIdAndExpenseId(AuthContext.getCurrentUserId(), expenseId)
                .ifPresent(match -> {
                    manualMatchExpenseRepository.delete(match);
                    
                    // Mark the expense as unreconciled
                    expenseRepository.findById(expenseId).ifPresent(expense -> {
                        expense.setReconciled(false);
                        expenseRepository.save(expense);
                    });
                });
    }

    public List<ManualMatchExpense> getManualExpenseMatches() {
        return manualMatchExpenseRepository.findByUserId(AuthContext.getCurrentUserId());
    }

    // Income reconciliation methods
    public ManualMatchIncome createManualIncomeMatch(Long incomeId, String transactionId) {
        ManualMatchIncome match = new ManualMatchIncome(AuthContext.getCurrentUserId(), incomeId, transactionId);
        
        // Mark the income as reconciled
        incomeRepository.findById(incomeId).ifPresent(income -> {
            income.setReconciled(true);
            incomeRepository.save(income);
        });
        
        return manualMatchIncomeRepository.save(match);
    }

    public void deleteManualIncomeMatch(Long incomeId) {
        manualMatchIncomeRepository.findByUserIdAndIncomeId(AuthContext.getCurrentUserId(), incomeId)
                .ifPresent(match -> {
                    manualMatchIncomeRepository.delete(match);
                    
                    // Mark the income as unreconciled
                    incomeRepository.findById(incomeId).ifPresent(income -> {
                        income.setReconciled(false);
                        incomeRepository.save(income);
                    });
                });
    }

    public List<ManualMatchIncome> getManualIncomeMatches() {
        return manualMatchIncomeRepository.findByUserId(AuthContext.getCurrentUserId());
    }

    public ReconciliationSummary getReconciliationSummary() {
        try {
            // Get bank transactions for the last 90 days
            LocalDate endDate = LocalDate.now();
            LocalDate startDate = endDate.minusDays(90);
            DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;
            List<PlaidTransaction> transactions = plaidService.getTransactions(
                startDate.format(formatter), 
                endDate.format(formatter)
            );
            
            // Get all manual matches
            List<ManualMatchExpense> expenseMatches = manualMatchExpenseRepository.findByUserId(AuthContext.getCurrentUserId());
            List<ManualMatchIncome> incomeMatches = manualMatchIncomeRepository.findByUserId(AuthContext.getCurrentUserId());
            
            // Calculate counts
            int totalExpenses = (int) expenseRepository.count();
            int totalIncome = (int) incomeRepository.count();
            int matchedExpenses = expenseMatches.size();
            int matchedIncome = incomeMatches.size();
            int unmatchedExpenses = totalExpenses - matchedExpenses;
            int unmatchedIncome = totalIncome - matchedIncome;
            
            // Count matched bank transactions
            Set<String> matchedTransactionIds = new HashSet<>();
            expenseMatches.forEach(m -> matchedTransactionIds.add(m.getTransactionId()));
            incomeMatches.forEach(m -> matchedTransactionIds.add(m.getTransactionId()));
            
            int matchedBankTransactions = matchedTransactionIds.size();
            int unmatchedBankTransactions = transactions.size() - matchedBankTransactions;
            
            // Map to existing ReconciliationSummary fields
            // matchedCount = total matched (expenses + income)
            // unmatchedPaymentCount = unmatched income and expenses combined
            // totalPayments = total income and expenses combined
            ReconciliationSummary summary = new ReconciliationSummary();
            summary.setMatchedCount(matchedExpenses + matchedIncome);
            summary.setUnmatchedPaymentCount(unmatchedExpenses + unmatchedIncome);
            summary.setUnmatchedBankCount(unmatchedBankTransactions);
            summary.setTotalPayments(totalExpenses + totalIncome);
            summary.setTotalBankTransactions(transactions.size());
            summary.setRunAt(Instant.now());
            summary.setStatus("SUCCESS");
            summary.setHasIssues(unmatchedExpenses > 0 || unmatchedIncome > 0 || unmatchedBankTransactions > 0);
            summary.setStartDate(startDate);
            summary.setEndDate(endDate);
            
            // Save to database
            ReconciliationRun run = new ReconciliationRun();
            run.setUserId(AuthContext.getCurrentUserId());
            run.setRunAt(Instant.now());
            run.setStartDate(startDate);
            run.setEndDate(endDate);
            run.setMatchedCount(matchedExpenses + matchedIncome);
            run.setUnmatchedPaymentCount(unmatchedExpenses + unmatchedIncome);
            run.setUnmatchedBankCount(unmatchedBankTransactions);
            run.setTotalPayments(totalExpenses + totalIncome);
            run.setTotalBankTransactions(transactions.size());
            run.setStatus("SUCCESS");
            
            ReconciliationRun savedRun = runRepository.save(run);
            summary.setId(savedRun.getId());
            
            return summary;
        } catch (Exception e) {
            throw new RuntimeException("Failed to get reconciliation summary", e);
        }
    }
}
