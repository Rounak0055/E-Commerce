package com.paintandpetals.service;

import com.paintandpetals.dto.response.CartResponse;
import com.paintandpetals.entity.Product;
import com.paintandpetals.entity.User;
import com.paintandpetals.entity.enums.Role;
import com.paintandpetals.exception.BadRequestException;
import com.paintandpetals.exception.ResourceNotFoundException;
import com.paintandpetals.repository.ProductRepository;
import com.paintandpetals.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public User getVendor(Long vendorId) {
        User vendor = userRepository.findById(vendorId)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor not found"));
        if (vendor.getRole() != Role.VENDOR) {
            throw new BadRequestException("Invalid vendor");
        }
        return vendor;
    }

    @Transactional
    public Product lockAndReserveStock(Long productId, int quantity) {
        Product product = productRepository.findByIdForUpdate(productId)
                .filter(Product::getActive)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + productId));

        if (product.getStockQuantity() < quantity) {
            throw new BadRequestException("Insufficient stock for: " + product.getName());
        }

        product.setStockQuantity(product.getStockQuantity() - quantity);
        return productRepository.save(product);
    }
}
