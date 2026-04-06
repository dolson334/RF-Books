package com.rfbooks.controllers;

import com.rfbooks.dtos.ExpenseRequest;
import com.rfbooks.dtos.ExpenseImportRequest;
import com.rfbooks.dtos.IncomeImportResponse;
import com.rfbooks.entities.Expense;
import com.rfbooks.enums.CategoryValidator;
import com.rfbooks.services.ExpenseService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

@RestController
@RequestMapping("/api/expenses")
public class ExpenseController {

    private final ExpenseService expenseService;

    public ExpenseController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    @GetMapping
    public ResponseEntity<?> getAllExpenses(
            @RequestParam(required = false) String resortAlias,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        
        if (page != null && size != null) {
            PageRequest pageReq = PageRequest.of(page, Math.min(size, 200), Sort.by("expenseDate").descending());
            if (startDate != null && endDate != null) {
                return ResponseEntity.ok(expenseService.getExpensesByDateRange(startDate, endDate, pageReq));
            }
            return ResponseEntity.ok(expenseService.getAllExpenses(pageReq));
        }

        if (category != null && !category.isEmpty()) {
            if (startDate != null && endDate != null) {
                return ResponseEntity.ok(expenseService.getExpensesByDateRangeAndCategory(startDate, endDate, category));
            }
            return ResponseEntity.ok(expenseService.getExpensesByCategory(category));
        }

        if (startDate != null && endDate != null) {
            return ResponseEntity.ok(expenseService.getExpensesByDateRange(startDate, endDate));
        }
        return ResponseEntity.ok(expenseService.getAllExpenses());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Expense> getExpenseById(
            @RequestParam(required = false) String resortAlias,
            @PathVariable Long id) {
        return expenseService.getExpenseById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> createExpense(
            @RequestParam(required = false) String resortAlias,
            @RequestBody ExpenseRequest request) {
        if (!CategoryValidator.isValidExpenseCategory(request.getCategory())) {
            return ResponseEntity.badRequest().body("Invalid expense category: " + request.getCategory());
        }
        if (!CategoryValidator.isValidPaymentMethod(request.getPaymentMethod())) {
            return ResponseEntity.badRequest().body("Invalid payment method: " + request.getPaymentMethod());
        }
        Expense expense = new Expense();
        expense.setExpenseDate(request.getExpenseDate());
        expense.setAmount(request.getAmount());
        expense.setVendorName(request.getVendorName());
        expense.setCategory(request.getCategory());
        expense.setAccountId(request.getAccountId());
        expense.setPaymentMethod(request.getPaymentMethod());
        expense.setReferenceNumber(request.getReferenceNumber());
        expense.setDescription(request.getDescription());
        expense.setNotes(request.getNotes());
        
        Expense created = expenseService.createExpense(expense);
        return ResponseEntity.ok(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateExpense(
            @RequestParam(required = false) String resortAlias,
            @PathVariable Long id, 
            @RequestBody ExpenseRequest request) {
        if (!CategoryValidator.isValidExpenseCategory(request.getCategory())) {
            return ResponseEntity.badRequest().body("Invalid expense category: " + request.getCategory());
        }
        if (!CategoryValidator.isValidPaymentMethod(request.getPaymentMethod())) {
            return ResponseEntity.badRequest().body("Invalid payment method: " + request.getPaymentMethod());
        }
        Expense expense = new Expense();
        expense.setExpenseDate(request.getExpenseDate());
        expense.setAmount(request.getAmount());
        expense.setVendorName(request.getVendorName());
        expense.setCategory(request.getCategory());
        expense.setAccountId(request.getAccountId());
        expense.setPaymentMethod(request.getPaymentMethod());
        expense.setReferenceNumber(request.getReferenceNumber());
        expense.setDescription(request.getDescription());
        expense.setNotes(request.getNotes());
        
        Expense updated = expenseService.updateExpense(id, expense);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteExpense(
            @RequestParam(required = false) String resortAlias,
            @PathVariable Long id) {
        expenseService.deleteExpense(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/resolve")
    public ResponseEntity<Expense> resolveExpense(
            @RequestParam(required = false) String resortAlias,
            @PathVariable Long id) {
        return ResponseEntity.ok(expenseService.resolveExpense(id));
    }

    @DeleteMapping("/{id}/resolve")
    public ResponseEntity<Expense> unresolveExpense(
            @RequestParam(required = false) String resortAlias,
            @PathVariable Long id) {
        return ResponseEntity.ok(expenseService.unresolveExpense(id));
    }

    @PostMapping("/import")
    public ResponseEntity<IncomeImportResponse> importExpenses(
            @RequestParam(required = false) String resortAlias,
            @RequestBody List<ExpenseImportRequest> requests) {
        IncomeImportResponse response = expenseService.importExpenses(requests);
        return ResponseEntity.ok(response);
    }
}
