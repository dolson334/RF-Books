package com.rfbooks.repos;

import com.rfbooks.entities.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    
    List<Expense> findByUserId(String userId);
    
    @Query("SELECT e FROM Expense e WHERE e.userId = :userId AND e.expenseDate >= :startDate AND e.expenseDate <= :endDate ORDER BY e.expenseDate DESC")
    List<Expense> findByUserIdAndDateRange(
        @Param("userId") String userId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );
    
    List<Expense> findByUserIdOrderByExpenseDateDesc(String userId);
}
