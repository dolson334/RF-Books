package com.rfbooks.repos;

import com.rfbooks.entities.PaymentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<PaymentEntity, Long> {
    
    List<PaymentEntity> findByUserId(String userId);
    
    @Query("SELECT p FROM PaymentEntity p WHERE p.userId = :userId AND p.paymentDate >= :startDate AND p.paymentDate <= :endDate ORDER BY p.paymentDate DESC")
    List<PaymentEntity> findByUserIdAndDateRange(
        @Param("userId") String userId,
        @Param("startDate") Instant startDate,
        @Param("endDate") Instant endDate
    );
}
