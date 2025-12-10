package com.rfbooks.services;

import com.rfbooks.dtos.OnboardingProgressResponse;
import com.rfbooks.entities.OnboardingProgress;
import com.rfbooks.repos.ChartOfAccountRepository;
import com.rfbooks.repos.OnboardingProgressRepository;
import com.rfbooks.repos.ProductServiceRepository;
import com.rfbooks.repos.PlaidConnectionRepository;
import com.rfbooks.repos.TaxRateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OnboardingProgressService {

    private static final String DEFAULT_USER_ID = "default-user";

    private final OnboardingProgressRepository progressRepository;
    private final ChartOfAccountRepository chartOfAccountRepository;
    private final ProductServiceRepository productServiceRepository;
    private final PlaidConnectionRepository plaidConnectionRepository;
    private final TaxRateRepository taxRateRepository;

    @Autowired
    public OnboardingProgressService(
            OnboardingProgressRepository progressRepository,
            ChartOfAccountRepository chartOfAccountRepository,
            ProductServiceRepository productServiceRepository,
            PlaidConnectionRepository plaidConnectionRepository,
            TaxRateRepository taxRateRepository) {
        this.progressRepository = progressRepository;
        this.chartOfAccountRepository = chartOfAccountRepository;
        this.productServiceRepository = productServiceRepository;
        this.plaidConnectionRepository = plaidConnectionRepository;
        this.taxRateRepository = taxRateRepository;
    }

    @Transactional(readOnly = true)
    public OnboardingProgressResponse getProgress() {
        // Check actual data instead of relying on stored progress flags
        boolean hasChartOfAccounts = chartOfAccountRepository.countByUserId(DEFAULT_USER_ID) > 0;
        boolean hasProductsServices = productServiceRepository.countByUserId(DEFAULT_USER_ID) > 0;
        boolean hasTaxesConfigured = taxRateRepository.findByUserIdOrderByNameAsc(DEFAULT_USER_ID).size() > 0;
        boolean hasBankConnection = plaidConnectionRepository.findByUserId(DEFAULT_USER_ID)
                .map(conn -> conn.getAccessToken() != null && !conn.getAccessToken().isEmpty())
                .orElse(false);

        // Check if there's a stored progress record
        OnboardingProgress progress = progressRepository.findByUserId(DEFAULT_USER_ID)
                .orElseGet(() -> {
                    OnboardingProgress newProgress = new OnboardingProgress();
                    newProgress.setUserId(DEFAULT_USER_ID);
                    return newProgress;
                });

        // Update flags based on actual data
        boolean isComplete = progress.getCompleted() != null && progress.getCompleted();

        return new OnboardingProgressResponse(
                hasBankConnection,
                hasChartOfAccounts,
                hasProductsServices,
                hasTaxesConfigured,
                isComplete
        );
    }

    @Transactional
    public void markChartOfAccountsCreated() {
        OnboardingProgress progress = getOrCreateProgress();
        progress.setChartOfAccountsCreated(true);
        progressRepository.save(progress);
    }

    @Transactional
    public void markProductsServicesCreated() {
        OnboardingProgress progress = getOrCreateProgress();
        progress.setProductsServicesCreated(true);
        progressRepository.save(progress);
    }

    @Transactional
    public void markTaxesConfigured() {
        OnboardingProgress progress = getOrCreateProgress();
        progress.setTaxesConfigured(true);
        progressRepository.save(progress);
    }

    @Transactional
    public void markBankConnected() {
        OnboardingProgress progress = getOrCreateProgress();
        progress.setBankConnected(true);
        progressRepository.save(progress);
    }

    @Transactional
    public void completeOnboarding() {
        OnboardingProgress progress = getOrCreateProgress();
        progress.setCompleted(true);
        progressRepository.save(progress);
    }

    private OnboardingProgress getOrCreateProgress() {
        return progressRepository.findByUserId(DEFAULT_USER_ID)
                .orElseGet(() -> {
                    OnboardingProgress newProgress = new OnboardingProgress();
                    newProgress.setUserId(DEFAULT_USER_ID);
                    return progressRepository.save(newProgress);
                });
    }
}
