package com.rfbooks.repos;

import com.rfbooks.entities.Income;
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

    @Query("SELECT i FROM Income i WHERE i.userId = :userId AND i.incomeDate BETWEEN :startDate AND :endDate ORDER BY i.incomeDate DESC")
    List<Income> findByUserIdAndDateRange(@Param("userId") String userId, 
                                          @Param("startDate") LocalDate startDate, 
                                          @Param("endDate") LocalDate endDate);

    Optional<Income> findByUserIdAndExternalId(String userId, String externalId);
}
