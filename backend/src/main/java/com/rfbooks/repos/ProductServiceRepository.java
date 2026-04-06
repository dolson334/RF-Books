package com.rfbooks.repos;

import com.rfbooks.entities.ProductService;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProductServiceRepository extends JpaRepository<ProductService, Long> {
    List<ProductService> findByUserId(String userId);
    void deleteByUserId(String userId);
}
