// OnboardingController.java
package com.rfbooks.controllers;

import com.rfbooks.dtos.ChartOfAccountDTO;
import com.rfbooks.dtos.OnboardingProgressResponse;
import com.rfbooks.dtos.ProductServiceDTO;
import com.rfbooks.services.ChartOfAccountService;
import com.rfbooks.services.OnboardingProgressService;
import com.rfbooks.services.ProductServiceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/onboarding")
public class OnboardingController {

    private final ChartOfAccountService chartOfAccountService;
    private final ProductServiceService productServiceService;
    private final OnboardingProgressService onboardingProgressService;

    public OnboardingController(
            ChartOfAccountService chartOfAccountService,
            ProductServiceService productServiceService,
            OnboardingProgressService onboardingProgressService) {
        this.chartOfAccountService = chartOfAccountService;
        this.productServiceService = productServiceService;
        this.onboardingProgressService = onboardingProgressService;
    }

    // ========== ONBOARDING PROGRESS ==========

    @GetMapping("/progress")
    public ResponseEntity<OnboardingProgressResponse> getProgress() {
        OnboardingProgressResponse progress = onboardingProgressService.getProgress();
        return ResponseEntity.ok(progress);
    }

    @PostMapping("/complete")
    public ResponseEntity<Void> completeOnboarding() {
        onboardingProgressService.completeOnboarding();
        return ResponseEntity.ok().build();
    }

    // ========== CHART OF ACCOUNTS ==========

    @PostMapping("/chart-of-accounts")
    public ResponseEntity<Void> saveChartOfAccounts(@RequestBody List<ChartOfAccountDTO> accounts) {
        String userId = "default-user"; // TODO: Get from authentication
        chartOfAccountService.saveChartOfAccounts(userId, accounts);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/chart-of-accounts")
    public ResponseEntity<List<ChartOfAccountDTO>> getChartOfAccounts() {
        String userId = "default-user"; // TODO: Get from authentication
        List<ChartOfAccountDTO> accounts = chartOfAccountService.getChartOfAccounts(userId);
        return ResponseEntity.ok(accounts);
    }

    @GetMapping("/chart-of-accounts/{id}")
    public ResponseEntity<ChartOfAccountDTO> getAccountById(@PathVariable Long id) {
        ChartOfAccountDTO account = chartOfAccountService.getAccountById(id);
        return ResponseEntity.ok(account);
    }

    @DeleteMapping("/chart-of-accounts/{id}")
    public ResponseEntity<Void> deleteAccount(@PathVariable Long id) {
        String userId = "default-user"; // TODO: Get from authentication
        chartOfAccountService.deleteAccount(userId, id);
        return ResponseEntity.ok().build();
    }

    // ========== PRODUCTS & SERVICES ==========

    @PostMapping("/products-services")
    public ResponseEntity<Void> saveProductsServices(@RequestBody List<ProductServiceDTO> items) {
        String userId = "default-user"; // TODO: Get from authentication
        productServiceService.saveProductsServices(userId, items);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/products-services")
    public ResponseEntity<List<ProductServiceDTO>> getProductsServices() {
        String userId = "default-user"; // TODO: Get from authentication
        List<ProductServiceDTO> items = productServiceService.getProductsServices(userId);
        return ResponseEntity.ok(items);
    }

    @GetMapping("/products-services/{id}")
    public ResponseEntity<ProductServiceDTO> getProductServiceById(@PathVariable Long id) {
        ProductServiceDTO item = productServiceService.getById(id);
        return ResponseEntity.ok(item);
    }

    @DeleteMapping("/products-services/{id}")
    public ResponseEntity<Void> deleteProductService(@PathVariable Long id) {
        String userId = "default-user"; // TODO: Get from authentication
        productServiceService.delete(userId, id);
        return ResponseEntity.ok().build();
    }
}