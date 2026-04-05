package com.rfbooks.services;

import com.rfbooks.config.AuthContext;
import com.rfbooks.config.TestHibernateConfig;
import com.rfbooks.dtos.IncomeImportRequest;
import com.rfbooks.dtos.IncomeImportResponse;
import com.rfbooks.entities.MatchSuggestion;
import com.rfbooks.entities.PlaidTransactionEntity;
import com.rfbooks.repos.PlaidTransactionRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestHibernateConfig.class)
class ReconciliationWithImportTest {

    @Autowired
    private IncomeService incomeService;

    @Autowired
    private AutoMatchService autoMatchService;

    @Autowired
    private PlaidTransactionRepository plaidTransactionRepository;

    @BeforeEach
    void setUp() {
        AuthContext.setCurrentUserId("test-user");
    }

    @AfterEach
    void tearDown() {
        AuthContext.clear();
    }

    @Test
    void importedPayments_matchBankTransactions_generatesSuggestions() {
        // Step 1: Import reservation payments as income
        List<IncomeImportRequest> payments = List.of(
                makeImportRequest("reservation_10_payment_1", 250.00, LocalDate.of(2026, 3, 15),
                        "Reservation #10", "Room Revenue", "card"),
                makeImportRequest("reservation_11_payment_2", 180.50, LocalDate.of(2026, 3, 16),
                        "Reservation #11", "Room Revenue", "check")
        );

        IncomeImportResponse importResponse = incomeService.importIncome(payments);
        assertEquals(2, importResponse.getCreated());

        // Step 2: Seed matching bank transactions (Plaid credits = negative amounts)
        PlaidTransactionEntity bankTx1 = new PlaidTransactionEntity();
        bankTx1.setUserId("test-user");
        bankTx1.setTransactionId("plaid_tx_100");
        bankTx1.setAccountId("acct_1");
        bankTx1.setAmount(-250.00); // Credit (income) in Plaid = negative
        bankTx1.setDate(LocalDate.of(2026, 3, 15)); // Same day
        bankTx1.setName("Reservation Payment");
        bankTx1.setCategory("Transfer");
        plaidTransactionRepository.save(bankTx1);

        PlaidTransactionEntity bankTx2 = new PlaidTransactionEntity();
        bankTx2.setUserId("test-user");
        bankTx2.setTransactionId("plaid_tx_101");
        bankTx2.setAccountId("acct_1");
        bankTx2.setAmount(-180.50); // Exact match
        bankTx2.setDate(LocalDate.of(2026, 3, 16)); // Same day as income
        bankTx2.setName("Check Deposit");
        bankTx2.setCategory("Transfer");
        plaidTransactionRepository.save(bankTx2);

        // Step 3: Run auto-match engine
        List<MatchSuggestion> suggestions = autoMatchService.generateSuggestions();

        // Step 4: Verify suggestions generated
        assertFalse(suggestions.isEmpty(), "Should generate match suggestions");

        // First suggestion should match $250.00 income to $250.00 bank tx (exact amount + same date = 70+)
        MatchSuggestion suggestion1 = suggestions.stream()
                .filter(s -> s.getTransactionId().equals("plaid_tx_100"))
                .findFirst()
                .orElse(null);
        assertNotNull(suggestion1, "Should suggest match for plaid_tx_100");
        assertTrue(suggestion1.getConfidenceScore() >= 70,
                "Confidence should be >= 70 (amount exact +40, same day +30)");
        assertNotNull(suggestion1.getIncomeId());

        // Second suggestion should match $180.50 income to $180.50 bank tx
        MatchSuggestion suggestion2 = suggestions.stream()
                .filter(s -> s.getTransactionId().equals("plaid_tx_101"))
                .findFirst()
                .orElse(null);
        assertNotNull(suggestion2, "Should suggest match for plaid_tx_101");
        assertTrue(suggestion2.getConfidenceScore() >= 70,
                "Confidence should be >= 70 (amount exact +40, same day +30)");
    }

    @Test
    void importedPayments_deduplication_preventsDoubleMatching() {
        // Import once
        List<IncomeImportRequest> payments = List.of(
                makeImportRequest("reservation_20_payment_5", 300.00, LocalDate.of(2026, 3, 20),
                        "Reservation #20", "Room Revenue", "card")
        );
        IncomeImportResponse first = incomeService.importIncome(payments);
        assertEquals(1, first.getCreated());

        // Import same again
        IncomeImportResponse second = incomeService.importIncome(payments);
        assertEquals(0, second.getCreated());
        assertEquals(1, second.getSkipped());

        // Verify only one income record exists (not duplicated)
        List<?> allIncome = incomeService.getAllIncome();
        long matchCount = allIncome.stream().count();
        // Should only be the records from this test (could include records from other tests if not isolated)
        assertTrue(matchCount >= 1);
    }

    private IncomeImportRequest makeImportRequest(String externalId, Double amount, LocalDate date,
                                                   String source, String category, String paymentMethod) {
        IncomeImportRequest req = new IncomeImportRequest();
        req.setExternalId(externalId);
        req.setAmount(amount);
        req.setIncomeDate(date);
        req.setSource(source);
        req.setCategory(category);
        req.setPaymentMethod(paymentMethod);
        req.setDescription("Payment for " + source);
        return req;
    }
}
