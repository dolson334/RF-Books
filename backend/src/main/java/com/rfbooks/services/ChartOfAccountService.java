// ChartOfAccountService.java
package com.rfbooks.services;

import com.rfbooks.dtos.ChartOfAccountDTO;
import com.rfbooks.entities.ChartOfAccount;
import com.rfbooks.repos.ChartOfAccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ChartOfAccountService {

    private final ChartOfAccountRepository repository;

    public ChartOfAccountService(ChartOfAccountRepository repository) {
        this.repository = repository;
    }

    public List<ChartOfAccountDTO> getChartOfAccounts(String userId) {
        return repository.findByUserId(userId)
                .stream()
                .map(ChartOfAccountDTO::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public void saveChartOfAccounts(String userId, List<ChartOfAccountDTO> accountDTOs) {
        // Delete existing accounts for this user
        repository.deleteByUserId(userId);

        // Save new accounts
        List<ChartOfAccount> accounts = accountDTOs.stream()
                .map(dto -> {
                    ChartOfAccount account = dto.toEntity();
                    account.setUserId(userId);
                    return account;
                })
                .collect(Collectors.toList());

        repository.saveAll(accounts);
    }

    public ChartOfAccountDTO getAccountById(Long id) {
        return repository.findById(id)
                .map(ChartOfAccountDTO::new)
                .orElseThrow(() -> new RuntimeException("Account not found with id: " + id));
    }

    @Transactional
    public void deleteAccount(String userId, Long id) {
        ChartOfAccount account = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        if (!account.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }

        repository.deleteById(id);
    }
}