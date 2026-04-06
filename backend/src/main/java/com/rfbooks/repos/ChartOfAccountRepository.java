package com.rfbooks.repos;

import com.rfbooks.entities.ChartOfAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ChartOfAccountRepository extends JpaRepository<ChartOfAccount, Long> {
    List<ChartOfAccount> findByUserId(String userId);
    void deleteByUserId(String userId);
}
