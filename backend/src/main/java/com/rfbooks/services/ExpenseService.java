package com.rfbooks.services;

import com.rfbooks.config.AuthContext;
import com.rfbooks.dtos.ExpenseImportRequest;
import com.rfbooks.dtos.IncomeImportResponse;
import com.rfbooks.entities.Expense;
import com.rfbooks.enums.CategoryValidator;
import com.rfbooks.repos.ExpenseRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class ExpenseService {

    private final ExpenseRepository expenseRepository;

    public ExpenseService(ExpenseRepository expenseRepository) {
        this.expenseRepository = expenseRepository;
    }

    public List<Expense> getAllExpenses() {
        return expenseRepository.findByUserIdOrderByExpenseDateDesc(AuthContext.getCurrentUserId());
    }

    public Page<Expense> getAllExpenses(Pageable pageable) {
        return expenseRepository.findByUserId(AuthContext.getCurrentUserId(), pageable);
    }

    public List<Expense> getExpensesByDateRange(LocalDate startDate, LocalDate endDate) {
        return expenseRepository.findByUserIdAndDateRange(AuthContext.getCurrentUserId(), startDate, endDate);
    }

    public List<Expense> getExpensesByCategory(String category) {
        return expenseRepository.findByUserIdAndCategory(AuthContext.getCurrentUserId(), category);
    }

    public List<Expense> getExpensesByDateRangeAndCategory(LocalDate startDate, LocalDate endDate, String category) {
        return expenseRepository.findByUserIdAndDateRangeAndCategory(AuthContext.getCurrentUserId(), startDate, endDate, category);
    }

    public Page<Expense> getExpensesByDateRange(LocalDate startDate, LocalDate endDate, Pageable pageable) {
        return expenseRepository.findByUserIdAndDateRange(AuthContext.getCurrentUserId(), startDate, endDate, pageable);
    }

    public Optional<Expense> getExpenseById(Long id) {
        return expenseRepository.findById(id);
    }

    public Expense createExpense(Expense expense) {
        expense.setUserId(AuthContext.getCurrentUserId());
        expense.setCreatedAt(Instant.now());
        expense.setUpdatedAt(Instant.now());
        return expenseRepository.save(expense);
    }

    public Expense resolveExpense(Long id) {
        return expenseRepository.findById(id)
                .map(expense -> {
                    expense.setResolved(true);
                    expense.setUpdatedAt(Instant.now());
                    return expenseRepository.save(expense);
                })
                .orElseThrow(() -> new RuntimeException("Expense not found with id: " + id));
    }

    public Expense unresolveExpense(Long id) {
        return expenseRepository.findById(id)
                .map(expense -> {
                    expense.setResolved(false);
                    expense.setUpdatedAt(Instant.now());
                    return expenseRepository.save(expense);
                })
                .orElseThrow(() -> new RuntimeException("Expense not found with id: " + id));
    }

    public Expense updateExpense(Long id, Expense updatedExpense) {
        return expenseRepository.findById(id)
                .map(expense -> {
                    expense.setExpenseDate(updatedExpense.getExpenseDate());
                    expense.setAmount(updatedExpense.getAmount());
                    expense.setVendorName(updatedExpense.getVendorName());
                    expense.setCategory(updatedExpense.getCategory());
                    expense.setAccountId(updatedExpense.getAccountId());
                    expense.setPaymentMethod(updatedExpense.getPaymentMethod());
                    expense.setReferenceNumber(updatedExpense.getReferenceNumber());
                    expense.setDescription(updatedExpense.getDescription());
                    expense.setNotes(updatedExpense.getNotes());
                    expense.setUpdatedAt(Instant.now());
                    return expenseRepository.save(expense);
                })
                .orElseThrow(() -> new RuntimeException("Expense not found with id: " + id));
    }

    public void deleteExpense(Long id) {
        expenseRepository.deleteById(id);
    }

    @Transactional
    public IncomeImportResponse importExpenses(List<ExpenseImportRequest> requests) {
        IncomeImportResponse response = new IncomeImportResponse();
        String userId = AuthContext.getCurrentUserId();

        for (int i = 0; i < requests.size(); i++) {
            ExpenseImportRequest req = requests.get(i);
            try {
                if (req.getExternalId() == null || req.getExternalId().isBlank()) {
                    response.addError("Item " + i + ": externalId is required");
                    continue;
                }
                if (req.getAmount() == null || req.getExpenseDate() == null) {
                    response.addError("Item " + i + " (" + req.getExternalId() + "): amount and expenseDate are required");
                    continue;
                }
                if (!CategoryValidator.isValidExpenseCategory(req.getCategory())) {
                    response.addError("Item " + i + " (" + req.getExternalId() + "): invalid category: " + req.getCategory());
                    continue;
                }

                Optional<Expense> existing = expenseRepository.findByUserIdAndExternalId(userId, req.getExternalId());
                if (existing.isPresent()) {
                    response.incrementSkipped();
                    continue;
                }

                Expense expense = new Expense();
                expense.setUserId(userId);
                expense.setExternalId(req.getExternalId());
                expense.setExpenseDate(req.getExpenseDate());
                expense.setAmount(req.getAmount());
                expense.setVendorName(req.getVendorName());
                expense.setCategory(req.getCategory());
                expense.setPaymentMethod(req.getPaymentMethod());
                expense.setReferenceNumber(req.getReferenceNumber());
                expense.setDescription(req.getDescription());
                expense.setNotes(req.getNotes());
                expense.setCreatedAt(Instant.now());
                expense.setUpdatedAt(Instant.now());
                expenseRepository.save(expense);
                response.incrementCreated();
            } catch (Exception e) {
                response.addError("Item " + i + " (" + req.getExternalId() + "): " + e.getMessage());
            }
        }

        return response;
    }
}
