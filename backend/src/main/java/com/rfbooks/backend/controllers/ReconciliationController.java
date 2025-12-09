package com.rfbooks.backend.controllers;

import com.rfbooks.backend.nonentities.Payment;
import com.rfbooks.backend.nonentities.ReconciliationMatch;
import com.rfbooks.backend.nonentities.ReconciliationRequest;
import com.rfbooks.backend.services.ReconciliationService;
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