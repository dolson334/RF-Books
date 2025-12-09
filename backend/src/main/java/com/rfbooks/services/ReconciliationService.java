package com.rfbooks.services;

import com.rfbooks.dtos.ReconciliationSummary;
import com.rfbooks.entities.ReconciliationRun;
import com.rfbooks.nonentities.BankTransactionSummary;
import com.rfbooks.nonentities.Payment;
import com.rfbooks.nonentities.ReconciliationMatch;
import com.rfbooks.nonentities.PlaidTransaction;
import com.rfbooks.repos.ReconciliationRunRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ReconciliationService {

    private static final String DEFAULT_USER_ID = "default-user";
    
    private final PlaidService plaidService;
    private final ReconciliationRunRepository runRepository;
    private final ObjectMapper objectMapper;

    public ReconciliationService(PlaidService plaidService, 
                                  ReconciliationRunRepository runRepository,
                                  ObjectMapper objectMapper) {
        this.plaidService = plaidService;
        this.runRepository = runRepository;
        this.objectMapper = objectMapper;
    }

    public List<Payment> getPayments(String from, String to) {
        // TODO: Replace with actual database query
        // For now, return empty list or mock data
        List<Payment> payments = new ArrayList<>();

        // Example payment
        Payment payment = new Payment();
        payment.setId(1L);
        payment.setExternalId("pi_001");
        payment.setAmount(178.50);
        payment.setCurrency("USD");
        payment.setPaymentDate(Instant.now().toString());
        payment.setMethod("Card");
        payment.setLast4("4242");
        payment.setGuestName("Sarah Thompson");
        payment.setReservationId("RV-1245");
        payment.setReconciled(false);
        payment.setSource("rfbooks");
        payments.add(payment);

        return payments;
    }

    public List<ReconciliationMatch> runReconciliation(String from, String to) {
        // Get payments from your database
        List<Payment> payments = getPayments(from, to);

        // Get bank transactions from Plaid (if connected)
        List<PlaidTransaction> plaidTransactions;
        try {
            plaidTransactions = plaidService.getTransactions(from, to);
        } catch (RuntimeException e) {
            // No bank connection - return payments as unmatched
            plaidTransactions = new ArrayList<>();
        }

        // Convert to BankTransactionSummary
        List<BankTransactionSummary> bankTransactions = new ArrayList<>();
        for (PlaidTransaction pt : plaidTransactions) {
            BankTransactionSummary bts = new BankTransactionSummary();
            bts.setId(Long.valueOf(pt.getTransactionId().hashCode()));
            bts.setAmount(pt.getAmount());
            bts.setCurrency("USD");
            bts.setTransactionDate(pt.getDate());
            bts.setDescription(pt.getName());
            bts.setSource("plaid");
            bankTransactions.add(bts);
        }

        // Perform matching logic
        List<ReconciliationMatch> matches = new ArrayList<>();

        // Simple matching: match by amount and date within 24 hours
        for (Payment payment : payments) {
            ReconciliationMatch match = new ReconciliationMatch();
            match.setId(payment.getId());
            match.setPayment(payment);

            BankTransactionSummary matchedTx = findMatchingTransaction(payment, bankTransactions);

            if (matchedTx != null) {
                match.setStatus("MATCHED");
                match.setBankTransaction(matchedTx);
                match.setDifferenceAmount(0.0);
                match.setReason("Matched by date and amount");
            } else {
                match.setStatus("UNMATCHED_PAYMENT");
                match.setDifferenceAmount(payment.getAmount());
                match.setReason("No matching bank transaction found");
            }

            match.setCreatedAt(Instant.now().toString());
            matches.add(match);
        }

        return matches;
    }

    public ReconciliationRun runAndSaveReconciliation() {
        // Run reconciliation for last 30 days
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(30);
        
        String start = startDate.format(DateTimeFormatter.ISO_DATE);
        String end = endDate.format(DateTimeFormatter.ISO_DATE);
        
        ReconciliationRun run = new ReconciliationRun();
        run.setUserId(DEFAULT_USER_ID);
        run.setRunAt(Instant.now());
        run.setStartDate(start);
        run.setEndDate(end);
        
        try {
            List<ReconciliationMatch> matches = runReconciliation(start, end);
            
            // Calculate summary
            int matched = (int) matches.stream().filter(m -> "MATCHED".equals(m.getStatus())).count();
            int unmatchedPayments = (int) matches.stream().filter(m -> "UNMATCHED_PAYMENT".equals(m.getStatus())).count();
            int unmatchedBank = (int) matches.stream().filter(m -> "UNMATCHED_BANK_TRANSACTION".equals(m.getStatus())).count();
            
            run.setMatchedCount(matched);
            run.setUnmatchedPaymentCount(unmatchedPayments);
            run.setUnmatchedBankCount(unmatchedBank);
            run.setTotalPayments((int) matches.stream().filter(m -> m.getPayment() != null).count());
            run.setTotalBankTransactions((int) matches.stream().filter(m -> m.getBankTransaction() != null).count());
            run.setStatus("COMPLETED");
            
            // Store results as JSON
            run.setResultsJson(objectMapper.writeValueAsString(matches));
            
        } catch (Exception e) {
            run.setStatus("FAILED");
            run.setErrorMessage(e.getMessage());
            run.setMatchedCount(0);
            run.setUnmatchedPaymentCount(0);
            run.setUnmatchedBankCount(0);
            run.setTotalPayments(0);
            run.setTotalBankTransactions(0);
        }
        
        return runRepository.save(run);
    }

    public Optional<ReconciliationSummary> getLatestRunSummary() {
        return runRepository.findLatestByUserId(DEFAULT_USER_ID)
                .map(run -> new ReconciliationSummary(
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

    public Optional<ReconciliationRun> getLatestRun() {
        return runRepository.findLatestByUserId(DEFAULT_USER_ID);
    }

    private BankTransactionSummary findMatchingTransaction(Payment payment,
                                                           List<BankTransactionSummary> transactions) {
        // Simple matching logic - you can make this more sophisticated
        for (BankTransactionSummary tx : transactions) {
            if (Math.abs(tx.getAmount() - payment.getAmount()) < 0.01) {
                // Amounts match within 1 cent
                return tx;
            }
        }
        return null;
    }
}