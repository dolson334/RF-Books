package com.rfbooks.controllers;

import com.rfbooks.dtos.ChartOfAccountRequest;
import com.rfbooks.dtos.ProductServiceRequest;
import com.rfbooks.entities.ChartOfAccount;
import com.rfbooks.entities.OnboardingProgress;
import com.rfbooks.entities.ProductService;
import com.rfbooks.services.OnboardingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/onboarding")
public class OnboardingController {

    private final OnboardingService onboardingService;

    public OnboardingController(OnboardingService onboardingService) {
        this.onboardingService = onboardingService;
    }

    @GetMapping("/chart-of-accounts")
    public ResponseEntity<List<ChartOfAccount>> getChartOfAccounts() {
        return ResponseEntity.ok(onboardingService.getChartOfAccounts());
    }

    @PostMapping("/chart-of-accounts")
    public ResponseEntity<Void> saveChartOfAccounts(@RequestBody List<ChartOfAccountRequest> requests) {
        onboardingService.saveChartOfAccounts(requests);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/products-services")
    public ResponseEntity<List<ProductService>> getProductsServices() {
        return ResponseEntity.ok(onboardingService.getProductsServices());
    }

    @PostMapping("/products-services")
    public ResponseEntity<Void> saveProductsServices(@RequestBody List<ProductServiceRequest> requests) {
        onboardingService.saveProductsServices(requests);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/progress")
    public ResponseEntity<OnboardingProgress> getProgress() {
        return ResponseEntity.ok(onboardingService.getProgress());
    }

    @PostMapping("/complete")
    public ResponseEntity<Void> completeOnboarding() {
        onboardingService.completeOnboarding();
        return ResponseEntity.noContent().build();
    }
}
