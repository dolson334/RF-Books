package com.rfbooks.services;

import com.rfbooks.config.AuthContext;
import com.rfbooks.entities.Income;
import com.rfbooks.repos.IncomeRepository;
import org.springframework.stereotype.Service;

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

    public List<Income> getIncomeByDateRange(LocalDate startDate, LocalDate endDate) {
        return incomeRepository.findByUserIdAndDateRange(AuthContext.getCurrentUserId(), startDate, endDate);
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
}
