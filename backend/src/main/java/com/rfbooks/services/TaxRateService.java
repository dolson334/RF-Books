package com.rfbooks.services;

import com.rfbooks.entities.TaxRate;
import com.rfbooks.repos.TaxRateRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class TaxRateService {

    private static final String DEFAULT_USER_ID = "default-user";
    private final TaxRateRepository taxRateRepository;

    public TaxRateService(TaxRateRepository taxRateRepository) {
        this.taxRateRepository = taxRateRepository;
    }

    public List<TaxRate> getAllTaxRates() {
        return taxRateRepository.findByUserIdOrderByNameAsc(DEFAULT_USER_ID);
    }

    public List<TaxRate> getActiveTaxRates() {
        return taxRateRepository.findByUserIdAndIsActiveTrue(DEFAULT_USER_ID);
    }

    @Transactional
    public List<TaxRate> saveTaxRates(List<TaxRate> taxRates) {
        for (TaxRate taxRate : taxRates) {
            taxRate.setUserId(DEFAULT_USER_ID);
            taxRate.setUpdatedAt(Instant.now());
            if (taxRate.getCreatedAt() == null) {
                taxRate.setCreatedAt(Instant.now());
            }
        }
        return taxRateRepository.saveAll(taxRates);
    }

    @Transactional
    public void deleteTaxRate(Long id) {
        taxRateRepository.deleteById(id);
    }
}
