package com.rfbooks.repos;

import com.rfbooks.entities.Expense;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    
    List<Expense> findByUserId(String userId);

    Page<Expense> findByUserId(String userId, Pageable pageable);
    
    @Query("SELECT e FROM Expense e WHERE e.userId = :userId AND e.expenseDate >= :startDate AND e.expenseDate <= :endDate ORDER BY e.expenseDate DESC")
    List<Expense> findByUserIdAndDateRange(
        @Param("userId") String userId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    @Query("SELECT e FROM Expense e WHERE e.userId = :userId AND e.expenseDate >= :startDate AND e.expenseDate <= :endDate")
    Page<Expense> findByUserIdAndDateRange(
        @Param("userId") String userId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate,
        Pageable pageable
    );
    
    List<Expense> findByUserIdOrderByExpenseDateDesc(String userId);

    Optional<Expense> findByUserIdAndExternalId(String userId, String externalId);

    @Query("SELECT e FROM Expense e WHERE e.userId = :userId AND e.category = :category ORDER BY e.expenseDate DESC")
    List<Expense> findByUserIdAndCategory(@Param("userId") String userId, @Param("category") String category);

    @Query("SELECT e FROM Expense e WHERE e.userId = :userId AND e.expenseDate >= :startDate AND e.expenseDate <= :endDate AND e.category = :category ORDER BY e.expenseDate DESC")
    List<Expense> findByUserIdAndDateRangeAndCategory(
        @Param("userId") String userId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate,
        @Param("category") String category
    );

    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e WHERE e.userId = :userId AND e.expenseDate BETWEEN :startDate AND :endDate")
    java.math.BigDecimal sumAmountByUserIdAndDateRange(@Param("userId") String userId,
                                                       @Param("startDate") LocalDate startDate,
                                                       @Param("endDate") LocalDate endDate);

    @Query("SELECT COUNT(e) FROM Expense e WHERE e.userId = :userId AND e.expenseDate BETWEEN :startDate AND :endDate")
    long countByUserIdAndDateRange(@Param("userId") String userId,
                                   @Param("startDate") LocalDate startDate,
                                   @Param("endDate") LocalDate endDate);

    @Query("SELECT COUNT(e) FROM Expense e WHERE e.userId = :userId AND e.expenseDate BETWEEN :startDate AND :endDate AND e.reconciled = true")
    long countReconciledByUserIdAndDateRange(@Param("userId") String userId,
                                             @Param("startDate") LocalDate startDate,
                                             @Param("endDate") LocalDate endDate);

    @Query("SELECT e.category, SUM(e.amount), COUNT(e) FROM Expense e WHERE e.userId = :userId AND e.expenseDate BETWEEN :startDate AND :endDate GROUP BY e.category ORDER BY SUM(e.amount) DESC")
    List<Object[]> sumByCategory(@Param("userId") String userId,
                                  @Param("startDate") LocalDate startDate,
                                  @Param("endDate") LocalDate endDate);
}
