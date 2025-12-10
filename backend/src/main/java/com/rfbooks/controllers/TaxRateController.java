package com.rfbooks.controllers;

import com.rfbooks.entities.TaxRate;
import com.rfbooks.services.TaxRateService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tax-rates")
@CrossOrigin(origins = "*")
public class TaxRateController {

    private final TaxRateService taxRateService;

    public TaxRateController(TaxRateService taxRateService) {
        this.taxRateService = taxRateService;
    }

    @GetMapping
    public ResponseEntity<List<TaxRate>> getAllTaxRates() {
        return ResponseEntity.ok(taxRateService.getAllTaxRates());
    }

    @GetMapping("/active")
    public ResponseEntity<List<TaxRate>> getActiveTaxRates() {
        return ResponseEntity.ok(taxRateService.getActiveTaxRates());
    }

    @PostMapping
    public ResponseEntity<List<TaxRate>> saveTaxRates(@RequestBody List<TaxRate> taxRates) {
        return ResponseEntity.ok(taxRateService.saveTaxRates(taxRates));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTaxRate(@PathVariable Long id) {
        taxRateService.deleteTaxRate(id);
        return ResponseEntity.noContent().build();
    }
}
