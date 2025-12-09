package com.rfbooks.repos;

import com.rfbooks.entities.ManualMatchIncome;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ManualMatchIncomeRepository extends JpaRepository<ManualMatchIncome, Long> {
    List<ManualMatchIncome> findByUserId(String userId);
    Optional<ManualMatchIncome> findByUserIdAndIncomeId(String userId, Long incomeId);
    Optional<ManualMatchIncome> findByUserIdAndTransactionId(String userId, String transactionId);
}
