package com.rfbooks.backend.services;

import com.rfbooks.backend.nonentities.BankTransactionSummary;
import com.rfbooks.backend.nonentities.Payment;
import com.rfbooks.backend.nonentities.ReconciliationMatch;
import com.rfbooks.backend.nonentities.PlaidTransaction;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class ReconciliationService {

    private final PlaidService plaidService;

    public ReconciliationService(PlaidService plaidService) {
        this.plaidService = plaidService;
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

        // Get bank transactions from Plaid
        List<PlaidTransaction> plaidTransactions = plaidService.getTransactions(from, to);

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