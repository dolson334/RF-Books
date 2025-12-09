package com.rfbooks.controllers;

import com.rfbooks.entities.ManualMatchExpense;
import com.rfbooks.entities.ManualMatchIncome;
import com.rfbooks.dtos.ManualMatchRequest;
import com.rfbooks.dtos.ReconciliationSummary;
import com.rfbooks.services.ReconciliationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/reconciliation")
public class ReconciliationController {

    private final ReconciliationService reconciliationService;

    public ReconciliationController(ReconciliationService reconciliationService) {
        this.reconciliationService = reconciliationService;
    }

    @GetMapping("/summary")
    public ResponseEntity<ReconciliationSummary> getReconciliationSummary() {
        ReconciliationSummary summary = reconciliationService.getReconciliationSummary();
        return ResponseEntity.ok(summary);
    }

    @PostMapping("/refresh")
    public ResponseEntity<ReconciliationSummary> refreshReconciliation() {
        ReconciliationSummary summary = reconciliationService.getReconciliationSummary();
        return ResponseEntity.ok(summary);
    }

    // Expense manual matching endpoints
    @PostMapping("/match/expense")
    public ResponseEntity<ManualMatchExpense> createManualExpenseMatch(@RequestBody ManualMatchRequest request) {
        ManualMatchExpense match = reconciliationService.createManualExpenseMatch(
                request.getExpenseId(),
                request.getTransactionId()
        );
        return ResponseEntity.ok(match);
    }

    @DeleteMapping("/match/expense/{expenseId}")
    public ResponseEntity<Void> deleteManualExpenseMatch(@PathVariable Long expenseId) {
        reconciliationService.deleteManualExpenseMatch(expenseId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/matches/expenses")
    public ResponseEntity<List<ManualMatchExpense>> getManualExpenseMatches() {
        return ResponseEntity.ok(reconciliationService.getManualExpenseMatches());
    }

    // Income manual matching endpoints
    @PostMapping("/match/income")
    public ResponseEntity<ManualMatchIncome> createManualIncomeMatch(@RequestBody ManualMatchRequest request) {
        ManualMatchIncome match = reconciliationService.createManualIncomeMatch(
                request.getIncomeId(),
                request.getTransactionId()
        );
        return ResponseEntity.ok(match);
    }

    @DeleteMapping("/match/income/{incomeId}")
    public ResponseEntity<Void> deleteManualIncomeMatch(@PathVariable Long incomeId) {
        reconciliationService.deleteManualIncomeMatch(incomeId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/matches/income")
    public ResponseEntity<List<ManualMatchIncome>> getManualIncomeMatches() {
        return ResponseEntity.ok(reconciliationService.getManualIncomeMatches());
    }
}

