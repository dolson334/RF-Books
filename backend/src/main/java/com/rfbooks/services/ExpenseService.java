package com.rfbooks.services;

import com.rfbooks.entities.Expense;
import com.rfbooks.repos.ExpenseRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class ExpenseService {

    private static final String DEFAULT_USER_ID = "default-user";
    
    private final ExpenseRepository expenseRepository;

    public ExpenseService(ExpenseRepository expenseRepository) {
        this.expenseRepository = expenseRepository;
    }

    public List<Expense> getAllExpenses() {
        return expenseRepository.findByUserIdOrderByExpenseDateDesc(DEFAULT_USER_ID);
    }

    public List<Expense> getExpensesByDateRange(LocalDate startDate, LocalDate endDate) {
        return expenseRepository.findByUserIdAndDateRange(DEFAULT_USER_ID, startDate, endDate);
    }

    public Optional<Expense> getExpenseById(Long id) {
        return expenseRepository.findById(id);
    }

    public Expense createExpense(Expense expense) {
        expense.setUserId(DEFAULT_USER_ID);
        expense.setCreatedAt(Instant.now());
        expense.setUpdatedAt(Instant.now());
        return expenseRepository.save(expense);
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
}
