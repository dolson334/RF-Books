package com.rfbooks.repos;

import com.rfbooks.entities.ManualMatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ManualMatchRepository extends JpaRepository<ManualMatch, Long> {
    List<ManualMatch> findByUserId(String userId);
    Optional<ManualMatch> findByUserIdAndPaymentId(String userId, String paymentId);
    Optional<ManualMatch> findByUserIdAndTransactionId(String userId, String transactionId);
    void deleteByUserIdAndPaymentId(String userId, String paymentId);
}
