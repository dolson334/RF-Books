package com.rfbooks.services;

import com.rfbooks.config.AuthContext;
import com.rfbooks.dtos.IncomeImportRequest;
import com.rfbooks.dtos.IncomeImportResponse;
import com.rfbooks.entities.Income;
import com.rfbooks.repos.IncomeRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class IncomeService {

    private final IncomeRepository incomeRepository;

    public IncomeService(IncomeRepository incomeRepository) {
        this.incomeRepository = incomeRepository;
    }

    public List<Income> getAllIncome() {
        return incomeRepository.findByUserIdOrderByIncomeDateDesc(AuthContext.getCurrentUserId());
    }

    public Page<Income> getAllIncome(Pageable pageable) {
        return incomeRepository.findByUserId(AuthContext.getCurrentUserId(), pageable);
    }

    public List<Income> getIncomeByDateRange(LocalDate startDate, LocalDate endDate) {
        return incomeRepository.findByUserIdAndDateRange(AuthContext.getCurrentUserId(), startDate, endDate);
    }

    public List<Income> getIncomeByCategory(String category) {
        return incomeRepository.findByUserIdAndCategory(AuthContext.getCurrentUserId(), category);
    }

    public List<Income> getIncomeByDateRangeAndCategory(LocalDate startDate, LocalDate endDate, String category) {
        return incomeRepository.findByUserIdAndDateRangeAndCategory(AuthContext.getCurrentUserId(), startDate, endDate, category);
    }

    public Page<Income> getIncomeByDateRange(LocalDate startDate, LocalDate endDate, Pageable pageable) {
        return incomeRepository.findByUserIdAndDateRange(AuthContext.getCurrentUserId(), startDate, endDate, pageable);
    }

    public Optional<Income> getIncomeById(Long id) {
        return incomeRepository.findById(id);
    }

    public Income createIncome(Income income) {
        income.setUserId(AuthContext.getCurrentUserId());
        income.setCreatedAt(Instant.now());
        income.setUpdatedAt(Instant.now());
        return incomeRepository.save(income);
    }

    public Income resolveIncome(Long id) {
        return incomeRepository.findById(id)
                .map(income -> {
                    income.setResolved(true);
                    income.setUpdatedAt(Instant.now());
                    return incomeRepository.save(income);
                })
                .orElseThrow(() -> new RuntimeException("Income not found with id: " + id));
    }

    public Income unresolveIncome(Long id) {
        return incomeRepository.findById(id)
                .map(income -> {
                    income.setResolved(false);
                    income.setUpdatedAt(Instant.now());
                    return incomeRepository.save(income);
                })
                .orElseThrow(() -> new RuntimeException("Income not found with id: " + id));
    }

    public Income updateIncome(Long id, Income updatedIncome) {
        return incomeRepository.findById(id)
                .map(income -> {
                    income.setIncomeDate(updatedIncome.getIncomeDate());
                    income.setAmount(updatedIncome.getAmount());
                    income.setSource(updatedIncome.getSource());
                    income.setCategory(updatedIncome.getCategory());
                    income.setAccountId(updatedIncome.getAccountId());
                    income.setPaymentMethod(updatedIncome.getPaymentMethod());
                    income.setReferenceNumber(updatedIncome.getReferenceNumber());
                    income.setDescription(updatedIncome.getDescription());
                    income.setNotes(updatedIncome.getNotes());
                    income.setReconciled(updatedIncome.getReconciled());
                    income.setUpdatedAt(Instant.now());
                    return incomeRepository.save(income);
                })
                .orElseThrow(() -> new RuntimeException("Income not found with id: " + id));
    }

    public void deleteIncome(Long id) {
        incomeRepository.deleteById(id);
    }

    @Transactional
    public IncomeImportResponse importIncome(List<IncomeImportRequest> requests) {
        IncomeImportResponse response = new IncomeImportResponse();
        String userId = AuthContext.getCurrentUserId();

        for (int i = 0; i < requests.size(); i++) {
            IncomeImportRequest req = requests.get(i);
            try {
                if (req.getExternalId() == null || req.getExternalId().isBlank()) {
                    response.addError("Item " + i + ": externalId is required");
                    continue;
                }
                if (req.getAmount() == null || req.getIncomeDate() == null) {
                    response.addError("Item " + i + " (" + req.getExternalId() + "): amount and incomeDate are required");
                    continue;
                }

                Optional<Income> existing = incomeRepository.findByUserIdAndExternalId(userId, req.getExternalId());
                if (existing.isPresent()) {
                    response.incrementSkipped();
                    continue;
                }

                Income income = new Income();
                income.setUserId(userId);
                income.setExternalId(req.getExternalId());
                income.setIncomeDate(req.getIncomeDate());
                income.setAmount(req.getAmount());
                income.setSource(req.getSource());
                income.setCategory(req.getCategory());
                income.setPaymentMethod(req.getPaymentMethod());
                income.setReferenceNumber(req.getReferenceNumber());
                income.setDescription(req.getDescription());
                income.setNotes(req.getNotes());
                income.setCreatedAt(Instant.now());
                income.setUpdatedAt(Instant.now());
                incomeRepository.save(income);
                response.incrementCreated();
            } catch (Exception e) {
                response.addError("Item " + i + " (" + req.getExternalId() + "): " + e.getMessage());
            }
        }

        return response;
    }
}
