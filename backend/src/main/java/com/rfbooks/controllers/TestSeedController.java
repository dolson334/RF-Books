package com.rfbooks.controllers;

import com.rfbooks.config.AuthContext;
import com.rfbooks.entities.PlaidTransactionEntity;
import com.rfbooks.repos.*;
import com.rfbooks.services.PlaidService;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
@Profile("local")
public class TestSeedController {

    private final PlaidTransactionRepository plaidTransactionRepository;
    private final IncomeRepository incomeRepository;
    private final ExpenseRepository expenseRepository;
    private final ManualMatchExpenseRepository manualMatchExpenseRepository;
    private final ManualMatchIncomeRepository manualMatchIncomeRepository;
    private final MatchSuggestionRepository matchSuggestionRepository;
    private final ReconciliationRunRepository reconciliationRunRepository;
    private final PlaidConnectionRepository plaidConnectionRepository;
    private final ChartOfAccountRepository chartOfAccountRepository;
    private final ProductServiceRepository productServiceRepository;
    private final OnboardingProgressRepository onboardingProgressRepository;
    private final PlaidService plaidService;

    public TestSeedController(PlaidTransactionRepository plaidTransactionRepository,
                               IncomeRepository incomeRepository,
                               ExpenseRepository expenseRepository,
                               ManualMatchExpenseRepository manualMatchExpenseRepository,
                               ManualMatchIncomeRepository manualMatchIncomeRepository,
                               MatchSuggestionRepository matchSuggestionRepository,
                               ReconciliationRunRepository reconciliationRunRepository,
                               PlaidConnectionRepository plaidConnectionRepository,
                               ChartOfAccountRepository chartOfAccountRepository,
                               ProductServiceRepository productServiceRepository,
                               OnboardingProgressRepository onboardingProgressRepository,
                               PlaidService plaidService) {
        this.plaidTransactionRepository = plaidTransactionRepository;
        this.incomeRepository = incomeRepository;
        this.expenseRepository = expenseRepository;
        this.manualMatchExpenseRepository = manualMatchExpenseRepository;
        this.manualMatchIncomeRepository = manualMatchIncomeRepository;
        this.matchSuggestionRepository = matchSuggestionRepository;
        this.reconciliationRunRepository = reconciliationRunRepository;
        this.plaidConnectionRepository = plaidConnectionRepository;
        this.chartOfAccountRepository = chartOfAccountRepository;
        this.productServiceRepository = productServiceRepository;
        this.onboardingProgressRepository = onboardingProgressRepository;
        this.plaidService = plaidService;
    }

    @PostMapping("/seed-bank-transaction")
    public ResponseEntity<Map<String, Object>> seedBankTransaction(@RequestBody Map<String, Object> data) {
        PlaidTransactionEntity entity = new PlaidTransactionEntity();
        entity.setUserId(AuthContext.getCurrentUserId());
        entity.setTransactionId((String) data.get("transactionId"));
        entity.setAccountId((String) data.getOrDefault("accountId", "test-account-1"));
        entity.setAmount(((Number) data.get("amount")).doubleValue());
        entity.setDate(LocalDate.parse((String) data.get("date")));
        entity.setName((String) data.get("name"));
        entity.setMerchantName((String) data.get("merchantName"));
        entity.setCategory((String) data.get("category"));
        entity.setPending(false);

        PlaidTransactionEntity saved = plaidTransactionRepository.save(entity);

        return ResponseEntity.ok(Map.of(
                "id", saved.getId(),
                "transactionId", saved.getTransactionId()
        ));
    }

    /**
     * Creates a Plaid sandbox public token for E2E testing.
     * This allows tests to complete the full Plaid connection flow
     * without the Link UI.
     */
    @PostMapping("/plaid-sandbox-token")
    public ResponseEntity<Map<String, String>> createSandboxToken() {
        String publicToken = plaidService.createSandboxPublicToken();
        return ResponseEntity.ok(Map.of("publicToken", publicToken));
    }

    @PostMapping("/cleanup")
    public ResponseEntity<Void> cleanup() {
        matchSuggestionRepository.deleteAll();
        manualMatchIncomeRepository.deleteAll();
        manualMatchExpenseRepository.deleteAll();
        reconciliationRunRepository.deleteAll();
        plaidTransactionRepository.deleteAll();
        incomeRepository.deleteAll();
        expenseRepository.deleteAll();
        plaidConnectionRepository.deleteAll();
        chartOfAccountRepository.deleteAll();
        productServiceRepository.deleteAll();
        onboardingProgressRepository.deleteAll();
        return ResponseEntity.ok().build();
    }
}
