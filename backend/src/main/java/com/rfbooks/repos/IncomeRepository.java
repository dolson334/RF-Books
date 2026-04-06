package com.rfbooks.repos;

import com.rfbooks.entities.Income;
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
public interface IncomeRepository extends JpaRepository<Income, Long> {

    List<Income> findByUserIdOrderByIncomeDateDesc(String userId);

    Page<Income> findByUserId(String userId, Pageable pageable);

    @Query("SELECT i FROM Income i WHERE i.userId = :userId AND i.incomeDate BETWEEN :startDate AND :endDate ORDER BY i.incomeDate DESC")
    List<Income> findByUserIdAndDateRange(@Param("userId") String userId, 
                                          @Param("startDate") LocalDate startDate, 
                                          @Param("endDate") LocalDate endDate);

    @Query("SELECT i FROM Income i WHERE i.userId = :userId AND i.incomeDate BETWEEN :startDate AND :endDate")
    Page<Income> findByUserIdAndDateRange(@Param("userId") String userId,
                                          @Param("startDate") LocalDate startDate,
                                          @Param("endDate") LocalDate endDate,
                                          Pageable pageable);

    Optional<Income> findByUserIdAndExternalId(String userId, String externalId);

    @Query("SELECT i FROM Income i WHERE i.userId = :userId AND i.category = :category ORDER BY i.incomeDate DESC")
    List<Income> findByUserIdAndCategory(@Param("userId") String userId, @Param("category") String category);

    @Query("SELECT i FROM Income i WHERE i.userId = :userId AND i.incomeDate BETWEEN :startDate AND :endDate AND i.category = :category ORDER BY i.incomeDate DESC")
    List<Income> findByUserIdAndDateRangeAndCategory(
        @Param("userId") String userId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate,
        @Param("category") String category
    );

    @Query("SELECT COALESCE(SUM(i.amount), 0) FROM Income i WHERE i.userId = :userId AND i.incomeDate BETWEEN :startDate AND :endDate")
    java.math.BigDecimal sumAmountByUserIdAndDateRange(@Param("userId") String userId,
                                                       @Param("startDate") LocalDate startDate,
                                                       @Param("endDate") LocalDate endDate);

    @Query("SELECT COUNT(i) FROM Income i WHERE i.userId = :userId AND i.incomeDate BETWEEN :startDate AND :endDate")
    long countByUserIdAndDateRange(@Param("userId") String userId,
                                   @Param("startDate") LocalDate startDate,
                                   @Param("endDate") LocalDate endDate);

    @Query("SELECT COUNT(i) FROM Income i WHERE i.userId = :userId AND i.incomeDate BETWEEN :startDate AND :endDate AND i.reconciled = true")
    long countReconciledByUserIdAndDateRange(@Param("userId") String userId,
                                             @Param("startDate") LocalDate startDate,
                                             @Param("endDate") LocalDate endDate);

    @Query("SELECT i.category, SUM(i.amount), COUNT(i) FROM Income i WHERE i.userId = :userId AND i.incomeDate BETWEEN :startDate AND :endDate GROUP BY i.category ORDER BY SUM(i.amount) DESC")
    List<Object[]> sumByCategory(@Param("userId") String userId,
                                 @Param("startDate") LocalDate startDate,
                                 @Param("endDate") LocalDate endDate);
}
