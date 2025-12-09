package com.rfbooks.repos;

import com.rfbooks.entities.PlaidTransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface PlaidTransactionRepository extends JpaRepository<PlaidTransactionEntity, Long> {
    
    List<PlaidTransactionEntity> findByUserId(String userId);
    
    @Query("SELECT p FROM PlaidTransactionEntity p WHERE p.userId = :userId AND p.date >= :startDate AND p.date <= :endDate ORDER BY p.date DESC")
    List<PlaidTransactionEntity> findByUserIdAndDateRange(
        @Param("userId") String userId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );
}
