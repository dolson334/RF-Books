package com.rfbooks.controllers;

import com.rfbooks.dtos.ReconciliationSummary;
import com.rfbooks.nonentities.Payment;
import com.rfbooks.nonentities.ReconciliationMatch;
import com.rfbooks.nonentities.ReconciliationRequest;
import com.rfbooks.services.ReconciliationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/reconciliation")
public class ReconciliationController {

    private final ReconciliationService reconciliationService;
    private final ObjectMapper objectMapper;

    public ReconciliationController(ReconciliationService reconciliationService, ObjectMapper objectMapper) {
        this.reconciliationService = reconciliationService;
        this.objectMapper = objectMapper;
    }

    @GetMapping("/latest")
    public ResponseEntity<ReconciliationSummary> getLatestRun() {
        return reconciliationService.getLatestRunSummary()
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/latest/details")
    public ResponseEntity<List<ReconciliationMatch>> getLatestRunDetails() {
        try {
            return reconciliationService.getLatestRun()
                    .map(run -> {
                        try {
                            List<ReconciliationMatch> matches = objectMapper.readValue(
                                    run.getResultsJson(),
                                    objectMapper.getTypeFactory().constructCollectionType(List.class, ReconciliationMatch.class)
                            );
                            return ResponseEntity.ok(matches);
                        } catch (Exception e) {
                            return ResponseEntity.<List<ReconciliationMatch>>internalServerError().build();
                        }
                    })
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/run-now")
    public ResponseEntity<ReconciliationSummary> runNow() {
        var run = reconciliationService.runAndSaveReconciliation();
        return ResponseEntity.ok(new ReconciliationSummary(
                run.getId(),
                run.getRunAt(),
                run.getStartDate(),
                run.getEndDate(),
                run.getMatchedCount(),
                run.getUnmatchedPaymentCount(),
                run.getUnmatchedBankCount(),
                run.getTotalPayments(),
                run.getTotalBankTransactions(),
                run.getStatus(),
                run.getErrorMessage()
        ));
    }

    // Keep existing endpoints for backward compatibility
    @GetMapping("/payments")
    public ResponseEntity<List<Payment>> getPayments(
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to) {
        List<Payment> payments = reconciliationService.getPayments(from, to);
        return ResponseEntity.ok(payments);
    }

    @PostMapping("/run")
    public ResponseEntity<List<ReconciliationMatch>> runReconciliation(
            @RequestBody ReconciliationRequest request) {
        List<ReconciliationMatch> matches = reconciliationService.runReconciliation(
                request.getFrom(),
                request.getTo()
        );
        return ResponseEntity.ok(matches);
    }
}