package com.rfbooks.services;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class ReconciliationScheduler {

    private final ReconciliationService reconciliationService;

    public ReconciliationScheduler(ReconciliationService reconciliationService) {
        this.reconciliationService = reconciliationService;
    }

    // Disabled - reconciliation is now manual for income/expenses
    // Run every hour
    // @Scheduled(cron = "0 0 * * * *")
    // public void runScheduledReconciliation() {
    //     System.out.println("Running scheduled reconciliation...");
    //     try {
    //         reconciliationService.runAndSaveReconciliation();
    //         System.out.println("Scheduled reconciliation completed successfully");
    //     } catch (Exception e) {
    //         System.err.println("Scheduled reconciliation failed: " + e.getMessage());
    //     }
    // }
}
