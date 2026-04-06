package com.rfbooks.services;

import com.rfbooks.config.AuthContext;
import com.rfbooks.dtos.ChartOfAccountRequest;
import com.rfbooks.dtos.ProductServiceRequest;
import com.rfbooks.entities.ChartOfAccount;
import com.rfbooks.entities.OnboardingProgress;
import com.rfbooks.entities.ProductService;
import com.rfbooks.repos.ChartOfAccountRepository;
import com.rfbooks.repos.OnboardingProgressRepository;
import com.rfbooks.repos.ProductServiceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class OnboardingService {

    private final ChartOfAccountRepository chartOfAccountRepository;
    private final ProductServiceRepository productServiceRepository;
    private final OnboardingProgressRepository onboardingProgressRepository;

    public OnboardingService(ChartOfAccountRepository chartOfAccountRepository,
                             ProductServiceRepository productServiceRepository,
                             OnboardingProgressRepository onboardingProgressRepository) {
        this.chartOfAccountRepository = chartOfAccountRepository;
        this.productServiceRepository = productServiceRepository;
        this.onboardingProgressRepository = onboardingProgressRepository;
    }

    public List<ChartOfAccount> getChartOfAccounts() {
        return chartOfAccountRepository.findByUserId(AuthContext.getCurrentUserId());
    }

    @Transactional
    public void saveChartOfAccounts(List<ChartOfAccountRequest> requests) {
        String userId = AuthContext.getCurrentUserId();
        chartOfAccountRepository.deleteByUserId(userId);

        for (ChartOfAccountRequest req : requests) {
            ChartOfAccount account = new ChartOfAccount();
            account.setUserId(userId);
            account.setAccountNumber(req.getAccountNumber());
            account.setAccountName(req.getAccountName());
            account.setAccountType(req.getAccountType());
            account.setDescription(req.getDescription());
            chartOfAccountRepository.save(account);
        }

        OnboardingProgress progress = getOrCreateProgress(userId);
        progress.setChartOfAccountsCreated(true);
        onboardingProgressRepository.save(progress);
    }

    public List<ProductService> getProductsServices() {
        return productServiceRepository.findByUserId(AuthContext.getCurrentUserId());
    }

    @Transactional
    public void saveProductsServices(List<ProductServiceRequest> requests) {
        String userId = AuthContext.getCurrentUserId();
        productServiceRepository.deleteByUserId(userId);

        for (ProductServiceRequest req : requests) {
            ProductService ps = new ProductService();
            ps.setUserId(userId);
            ps.setName(req.getName());
            ps.setType(req.getType());
            ps.setDefaultPrice(req.getDefaultPrice());
            ps.setUnitOfMeasure(req.getUnitOfMeasure());
            ps.setDescription(req.getDescription());
            ps.setRevenueAccountId(req.getRevenueAccountId());
            productServiceRepository.save(ps);
        }

        OnboardingProgress progress = getOrCreateProgress(userId);
        progress.setProductsServicesCreated(true);
        onboardingProgressRepository.save(progress);
    }

    public OnboardingProgress getProgress() {
        return getOrCreateProgress(AuthContext.getCurrentUserId());
    }

    @Transactional
    public void completeOnboarding() {
        OnboardingProgress progress = getOrCreateProgress(AuthContext.getCurrentUserId());
        progress.setCompleted(true);
        onboardingProgressRepository.save(progress);
    }

    @Transactional
    public void markBankConnected() {
        OnboardingProgress progress = getOrCreateProgress(AuthContext.getCurrentUserId());
        progress.setBankConnected(true);
        onboardingProgressRepository.save(progress);
    }

    private OnboardingProgress getOrCreateProgress(String userId) {
        return onboardingProgressRepository.findByUserId(userId).orElseGet(() -> {
            OnboardingProgress p = new OnboardingProgress();
            p.setUserId(userId);
            return onboardingProgressRepository.save(p);
        });
    }
}
