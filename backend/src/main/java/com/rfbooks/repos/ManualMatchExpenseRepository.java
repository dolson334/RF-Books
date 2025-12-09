package com.rfbooks.repos;

import com.rfbooks.entities.ManualMatchExpense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ManualMatchExpenseRepository extends JpaRepository<ManualMatchExpense, Long> {
    List<ManualMatchExpense> findByUserId(String userId);
    Optional<ManualMatchExpense> findByUserIdAndExpenseId(String userId, Long expenseId);
    Optional<ManualMatchExpense> findByUserIdAndTransactionId(String userId, String transactionId);
}
