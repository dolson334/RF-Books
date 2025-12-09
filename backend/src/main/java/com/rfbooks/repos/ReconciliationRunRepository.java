package com.rfbooks.repos;

import com.rfbooks.entities.ReconciliationRun;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReconciliationRunRepository extends JpaRepository<ReconciliationRun, Long> {

    @Query("SELECT r FROM ReconciliationRun r WHERE r.userId = ?1 ORDER BY r.runAt DESC LIMIT 1")
    Optional<ReconciliationRun> findLatestByUserId(String userId);
}
