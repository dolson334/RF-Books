// ProductServiceRepository.java
package com.rfbooks.repos;

import com.rfbooks.enums.ItemType;
import com.rfbooks.entities.ProductService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductServiceRepository extends JpaRepository<ProductService, Long> {

    List<ProductService> findByUserId(String userId);

    long countByUserId(String userId);

    List<ProductService> findByUserIdAndType(String userId, ItemType type);

    Optional<ProductService> findByUserIdAndName(String userId, String name);

    void deleteByUserId(String userId);
}