package com.rfbooks.repos;

import com.rfbooks.entities.TaxRate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaxRateRepository extends JpaRepository<TaxRate, Long> {
    
    List<TaxRate> findByUserIdOrderByNameAsc(String userId);
    
    List<TaxRate> findByUserIdAndIsActiveTrue(String userId);
}
