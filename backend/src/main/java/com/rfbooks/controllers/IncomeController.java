package com.rfbooks.controllers;

import com.rfbooks.dtos.IncomeRequest;
import com.rfbooks.dtos.IncomeImportRequest;
import com.rfbooks.dtos.IncomeImportResponse;
import com.rfbooks.entities.Income;
import com.rfbooks.services.IncomeService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/income")
public class IncomeController {

    private final IncomeService incomeService;

    public IncomeController(IncomeService incomeService) {
        this.incomeService = incomeService;
    }

    @GetMapping
    public ResponseEntity<List<Income>> getAllIncome(
            @RequestParam(required = false) String resortAlias,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        if (startDate != null && endDate != null) {
            return ResponseEntity.ok(incomeService.getIncomeByDateRange(startDate, endDate));
        }
        return ResponseEntity.ok(incomeService.getAllIncome());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Income> getIncomeById(
            @RequestParam(required = false) String resortAlias,
            @PathVariable Long id) {
        return incomeService.getIncomeById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Income> createIncome(
            @RequestParam(required = false) String resortAlias,
            @RequestBody IncomeRequest request) {
        Income income = new Income();
        income.setIncomeDate(request.getIncomeDate());
        income.setAmount(request.getAmount());
        income.setSource(request.getSource());
        income.setCategory(request.getCategory());
        income.setAccountId(request.getAccountId());
        income.setPaymentMethod(request.getPaymentMethod());
        income.setReferenceNumber(request.getReferenceNumber());
        income.setDescription(request.getDescription());
        income.setNotes(request.getNotes());
        income.setReconciled(request.getReconciled() != null ? request.getReconciled() : false);

        Income savedIncome = incomeService.createIncome(income);
        return ResponseEntity.ok(savedIncome);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Income> updateIncome(
            @RequestParam(required = false) String resortAlias,
            @PathVariable Long id,
            @RequestBody IncomeRequest request) {
        Income income = new Income();
        income.setIncomeDate(request.getIncomeDate());
        income.setAmount(request.getAmount());
        income.setSource(request.getSource());
        income.setCategory(request.getCategory());
        income.setAccountId(request.getAccountId());
        income.setPaymentMethod(request.getPaymentMethod());
        income.setReferenceNumber(request.getReferenceNumber());
        income.setDescription(request.getDescription());
        income.setNotes(request.getNotes());
        income.setReconciled(request.getReconciled() != null ? request.getReconciled() : false);

        Income updatedIncome = incomeService.updateIncome(id, income);
        return ResponseEntity.ok(updatedIncome);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteIncome(
            @RequestParam(required = false) String resortAlias,
            @PathVariable Long id) {
        incomeService.deleteIncome(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/import")
    public ResponseEntity<IncomeImportResponse> importIncome(
            @RequestParam(required = false) String resortAlias,
            @RequestBody List<IncomeImportRequest> requests) {
        IncomeImportResponse response = incomeService.importIncome(requests);
        return ResponseEntity.ok(response);
    }
}
