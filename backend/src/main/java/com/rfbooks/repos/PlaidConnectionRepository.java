package com.rfbooks.repos;

import com.rfbooks.entities.PlaidConnection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface PlaidConnectionRepository extends JpaRepository<PlaidConnection, Long> {

    Optional<PlaidConnection> findByUserId(String userId);

    Optional<PlaidConnection> findByUserIdAndActiveTrue(String userId);

    Optional<PlaidConnection> findByItemId(String itemId);
}