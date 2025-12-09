// ProductServiceService.java
package com.rfbooks.backend.services;

import com.rfbooks.backend.entities.ProductService;
import com.rfbooks.backend.enums.ItemType;
import com.rfbooks.backend.dtos.ProductServiceDTO;
import com.rfbooks.backend.repos.ProductServiceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductServiceService {

    private final ProductServiceRepository repository;

    public ProductServiceService(ProductServiceRepository repository) {
        this.repository = repository;
    }

    public List<ProductServiceDTO> getProductsServices(String userId) {
        return repository.findByUserId(userId)
                .stream()
                .map(ProductServiceDTO::new)
                .collect(Collectors.toList());
    }

    public List<ProductServiceDTO> getByType(String userId, ItemType type) {
        return repository.findByUserIdAndType(userId, type)
                .stream()
                .map(ProductServiceDTO::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public void saveProductsServices(String userId, List<ProductServiceDTO> itemDTOs) {
        // Delete existing items for this user
        repository.deleteByUserId(userId);

        // Save new items
        List<ProductService> items = itemDTOs.stream()
                .map(dto -> {
                    ProductService item = dto.toEntity();
                    item.setUserId(userId);
                    return item;
                })
                .collect(Collectors.toList());

        repository.saveAll(items);
    }

    public ProductServiceDTO getById(Long id) {
        return repository.findById(id)
                .map(ProductServiceDTO::new)
                .orElseThrow(() -> new RuntimeException("Product/Service not found with id: " + id));
    }

    @Transactional
    public ProductServiceDTO createOrUpdate(String userId, ProductServiceDTO dto) {
        ProductService entity;

        if (dto.getId() != null) {
            // Update existing
            entity = repository.findById(dto.getId())
                    .orElseThrow(() -> new RuntimeException("Product/Service not found"));

            if (!entity.getUserId().equals(userId)) {
                throw new RuntimeException("Unauthorized");
            }

            entity.setName(dto.getName());
            entity.setType(dto.getType());
            entity.setDefaultPrice(dto.getDefaultPrice());
            entity.setUnitOfMeasure(dto.getUnitOfMeasure());
            entity.setDescription(dto.getDescription());
            entity.setRevenueAccountId(dto.getRevenueAccountId());
        } else {
            // Create new
            entity = dto.toEntity();
            entity.setUserId(userId);
        }

        entity = repository.save(entity);
        return new ProductServiceDTO(entity);
    }

    @Transactional
    public void delete(String userId, Long id) {
        ProductService entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product/Service not found"));

        if (!entity.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }

        repository.deleteById(id);
    }
}