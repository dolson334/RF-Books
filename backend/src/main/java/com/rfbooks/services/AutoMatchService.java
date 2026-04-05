package com.rfbooks.services;

import com.rfbooks.config.AuthContext;
import com.rfbooks.entities.*;
import com.rfbooks.repos.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AutoMatchService {

    private static final int THRESHOLD_HIGH = 70;
    private static final int SCORE_AMOUNT_EXACT = 40;
    private static final int SCORE_DATE_SAME = 30;
    private static final int SCORE_DATE_1DAY = 20;
    private static final int SCORE_DATE_3DAY = 10;
    private static final int SCORE_DATE_7DAY = 5;
    private static final int SCORE_DESCRIPTION = 20;
    private static final int SCORE_CATEGORY = 10;

    private final ExpenseRepository expenseRepository;
    private final IncomeRepository incomeRepository;
    private final PlaidTransactionRepository transactionRepository;
    private final ManualMatchExpenseRepository matchExpenseRepository;
    private final ManualMatchIncomeRepository matchIncomeRepository;
    private final MatchSuggestionRepository suggestionRepository;

    public AutoMatchService(ExpenseRepository expenseRepository,
                            IncomeRepository incomeRepository,
                            PlaidTransactionRepository transactionRepository,
                            ManualMatchExpenseRepository matchExpenseRepository,
                            ManualMatchIncomeRepository matchIncomeRepository,
                            MatchSuggestionRepository suggestionRepository) {
        this.expenseRepository = expenseRepository;
        this.incomeRepository = incomeRepository;
        this.transactionRepository = transactionRepository;
        this.matchExpenseRepository = matchExpenseRepository;
        this.matchIncomeRepository = matchIncomeRepository;
        this.suggestionRepository = suggestionRepository;
    }

    @Transactional
    public List<MatchSuggestion> generateSuggestions() {
        String userId = AuthContext.getCurrentUserId();

        // Clear old pending suggestions
        suggestionRepository.deleteByUserId(userId);

        // Get already-matched transaction IDs
        Set<String> matchedTxIds = new HashSet<>();
        matchExpenseRepository.findByUserId(userId).forEach(m -> matchedTxIds.add(m.getTransactionId()));
        matchIncomeRepository.findByUserId(userId).forEach(m -> matchedTxIds.add(m.getTransactionId()));

        // Get unmatched expenses and income
        List<Expense> unmatchedExpenses = expenseRepository.findByUserIdOrderByExpenseDateDesc(userId)
                .stream().filter(e -> !Boolean.TRUE.equals(e.getReconciled())).collect(Collectors.toList());
        List<Income> unmatchedIncome = incomeRepository.findByUserIdOrderByIncomeDateDesc(userId)
                .stream().filter(i -> !Boolean.TRUE.equals(i.getReconciled())).collect(Collectors.toList());

        // Get unmatched bank transactions
        List<PlaidTransactionEntity> allTransactions = transactionRepository.findByUserId(userId);
        List<PlaidTransactionEntity> unmatchedTransactions = allTransactions.stream()
                .filter(tx -> !matchedTxIds.contains(tx.getTransactionId()))
                .collect(Collectors.toList());

        List<MatchSuggestion> suggestions = new ArrayList<>();

        // Match expenses to debit transactions (negative amounts in Plaid = money out)
        Set<String> usedTxIds = new HashSet<>();
        for (Expense expense : unmatchedExpenses) {
            MatchSuggestion best = null;
            for (PlaidTransactionEntity tx : unmatchedTransactions) {
                if (usedTxIds.contains(tx.getTransactionId())) continue;
                // Plaid: positive = money out (debit), negative = money in (credit)
                // Expenses should match debits (positive amount in Plaid)
                if (tx.getAmount() <= 0) continue;

                int score = calculateScore(expense.getAmount(), tx.getAmount(),
                        expense.getExpenseDate(), tx.getDate(),
                        expense.getVendorName(), tx.getName(), tx.getMerchantName(),
                        expense.getCategory(), tx.getCategory());

                if (score >= THRESHOLD_HIGH && (best == null || score > best.getConfidenceScore())) {
                    best = new MatchSuggestion();
                    best.setUserId(userId);
                    best.setExpenseId(expense.getId());
                    best.setTransactionId(tx.getTransactionId());
                    best.setConfidenceScore(score);
                    best.setMatchReasons(buildReasons(expense.getAmount(), tx.getAmount(),
                            expense.getExpenseDate(), tx.getDate(),
                            expense.getVendorName(), tx.getName(), tx.getMerchantName(),
                            expense.getCategory(), tx.getCategory()));
                }
            }
            if (best != null) {
                suggestions.add(suggestionRepository.save(best));
                usedTxIds.add(best.getTransactionId());
            }
        }

        // Match income to credit transactions (negative amounts in Plaid = money in)
        for (Income income : unmatchedIncome) {
            MatchSuggestion best = null;
            for (PlaidTransactionEntity tx : unmatchedTransactions) {
                if (usedTxIds.contains(tx.getTransactionId())) continue;
                // Credits are negative in Plaid
                if (tx.getAmount() >= 0) continue;

                int score = calculateScore(income.getAmount(), Math.abs(tx.getAmount()),
                        income.getIncomeDate(), tx.getDate(),
                        income.getSource(), tx.getName(), tx.getMerchantName(),
                        income.getCategory(), tx.getCategory());

                if (score >= THRESHOLD_HIGH && (best == null || score > best.getConfidenceScore())) {
                    best = new MatchSuggestion();
                    best.setUserId(userId);
                    best.setIncomeId(income.getId());
                    best.setTransactionId(tx.getTransactionId());
                    best.setConfidenceScore(score);
                    best.setMatchReasons(buildReasons(income.getAmount(), Math.abs(tx.getAmount()),
                            income.getIncomeDate(), tx.getDate(),
                            income.getSource(), tx.getName(), tx.getMerchantName(),
                            income.getCategory(), tx.getCategory()));
                }
            }
            if (best != null) {
                suggestions.add(suggestionRepository.save(best));
                usedTxIds.add(best.getTransactionId());
            }
        }

        return suggestions;
    }

    public List<MatchSuggestion> getPendingSuggestions() {
        return suggestionRepository.findByUserIdAndStatusOrderByConfidenceScoreDesc(
                AuthContext.getCurrentUserId(), "PENDING");
    }

    @Transactional
    public void acceptSuggestion(Long suggestionId, ReconciliationService reconciliationService) {
        MatchSuggestion suggestion = suggestionRepository.findById(suggestionId)
                .orElseThrow(() -> new RuntimeException("Suggestion not found: " + suggestionId));

        if (suggestion.getExpenseId() != null) {
            ManualMatchExpense match = reconciliationService.createManualExpenseMatch(
                    suggestion.getExpenseId(), suggestion.getTransactionId());
            match.setConfidenceScore(suggestion.getConfidenceScore());
            match.setMatchType("AUTO");
            matchExpenseRepository.save(match);
        } else if (suggestion.getIncomeId() != null) {
            ManualMatchIncome match = reconciliationService.createManualIncomeMatch(
                    suggestion.getIncomeId(), suggestion.getTransactionId());
            match.setConfidenceScore(suggestion.getConfidenceScore());
            match.setMatchType("AUTO");
            matchIncomeRepository.save(match);
        }

        suggestion.setStatus("ACCEPTED");
        suggestionRepository.save(suggestion);
    }

    @Transactional
    public void rejectSuggestion(Long suggestionId) {
        MatchSuggestion suggestion = suggestionRepository.findById(suggestionId)
                .orElseThrow(() -> new RuntimeException("Suggestion not found: " + suggestionId));
        suggestion.setStatus("REJECTED");
        suggestionRepository.save(suggestion);
    }

    @Transactional
    public int acceptAllHighConfidence(ReconciliationService reconciliationService) {
        List<MatchSuggestion> pending = getPendingSuggestions();
        int accepted = 0;
        for (MatchSuggestion s : pending) {
            if (s.getConfidenceScore() >= THRESHOLD_HIGH) {
                acceptSuggestion(s.getId(), reconciliationService);
                accepted++;
            }
        }
        return accepted;
    }

    private int calculateScore(Double itemAmount, Double txAmount,
                               LocalDate itemDate, LocalDate txDate,
                               String itemName, String txName, String txMerchant,
                               String itemCategory, String txCategory) {
        int score = 0;

        // Amount match (+40)
        if (itemAmount != null && txAmount != null && Math.abs(itemAmount - txAmount) < 0.01) {
            score += SCORE_AMOUNT_EXACT;
        }

        // Date proximity (+30 max)
        if (itemDate != null && txDate != null) {
            long daysDiff = Math.abs(ChronoUnit.DAYS.between(itemDate, txDate));
            if (daysDiff == 0) score += SCORE_DATE_SAME;
            else if (daysDiff <= 1) score += SCORE_DATE_1DAY;
            else if (daysDiff <= 3) score += SCORE_DATE_3DAY;
            else if (daysDiff <= 7) score += SCORE_DATE_7DAY;
        }

        // Description similarity (+20)
        if (itemName != null) {
            String normalizedItem = itemName.toLowerCase().trim();
            if (txMerchant != null && txMerchant.toLowerCase().trim().contains(normalizedItem)) {
                score += SCORE_DESCRIPTION;
            } else if (txName != null && txName.toLowerCase().trim().contains(normalizedItem)) {
                score += SCORE_DESCRIPTION;
            } else if (txName != null && fuzzyMatch(normalizedItem, txName.toLowerCase().trim())) {
                score += SCORE_DESCRIPTION / 2;
            }
        }

        // Category hint (+10)
        if (itemCategory != null && txCategory != null
                && txCategory.toLowerCase().contains(itemCategory.toLowerCase())) {
            score += SCORE_CATEGORY;
        }

        return score;
    }

    private boolean fuzzyMatch(String a, String b) {
        // Simple word overlap check
        Set<String> wordsA = new HashSet<>(Arrays.asList(a.split("\\s+")));
        Set<String> wordsB = new HashSet<>(Arrays.asList(b.split("\\s+")));
        wordsA.retainAll(wordsB);
        return wordsA.size() >= 1 && wordsA.size() >= Math.min(wordsA.size(), wordsB.size()) / 2;
    }

    private String buildReasons(Double itemAmount, Double txAmount,
                                LocalDate itemDate, LocalDate txDate,
                                String itemName, String txName, String txMerchant,
                                String itemCategory, String txCategory) {
        List<String> reasons = new ArrayList<>();

        if (itemAmount != null && txAmount != null && Math.abs(itemAmount - txAmount) < 0.01) {
            reasons.add("Exact amount match");
        }
        if (itemDate != null && txDate != null) {
            long daysDiff = Math.abs(ChronoUnit.DAYS.between(itemDate, txDate));
            if (daysDiff == 0) reasons.add("Same date");
            else if (daysDiff <= 1) reasons.add("Within 1 day");
            else if (daysDiff <= 3) reasons.add("Within 3 days");
            else if (daysDiff <= 7) reasons.add("Within 7 days");
        }
        if (itemName != null && (txMerchant != null || txName != null)) {
            String normalizedItem = itemName.toLowerCase().trim();
            if ((txMerchant != null && txMerchant.toLowerCase().contains(normalizedItem))
                    || (txName != null && txName.toLowerCase().contains(normalizedItem))) {
                reasons.add("Name match");
            }
        }
        if (itemCategory != null && txCategory != null
                && txCategory.toLowerCase().contains(itemCategory.toLowerCase())) {
            reasons.add("Category match");
        }

        return String.join(", ", reasons);
    }
}
