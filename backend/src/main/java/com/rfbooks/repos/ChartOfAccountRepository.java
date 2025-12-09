// ChartOfAccountRepository.java
package com.rfbooks.repos;

import com.rfbooks.entities.ChartOfAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ChartOfAccountRepository extends JpaRepository<ChartOfAccount, Long> {

    List<ChartOfAccount> findByUserId(String userId);

    long countByUserId(String userId);

    Optional<ChartOfAccount> findByUserIdAndAccountNumber(String userId, String accountNumber);

    void deleteByUserId(String userId);
}