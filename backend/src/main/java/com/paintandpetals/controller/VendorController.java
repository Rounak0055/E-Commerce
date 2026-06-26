package com.paintandpetals.controller;

import com.paintandpetals.dto.request.CategoryRequest;
import com.paintandpetals.dto.request.OrderStatusUpdateRequest;
import com.paintandpetals.dto.request.ProductRequest;
import com.paintandpetals.dto.request.VendorProfileRequest;
import com.paintandpetals.dto.response.*;
import com.paintandpetals.entity.VendorOrder;
import com.paintandpetals.mapper.DtoMapper;
import com.paintandpetals.security.SecurityUtils;
import com.paintandpetals.service.OrderService;
import com.paintandpetals.service.ProductService;
import com.paintandpetals.service.VendorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/vendor")
@RequiredArgsConstructor
public class VendorController {

    private final VendorService vendorService;
    private final ProductService productService;
    private final OrderService orderService;
    private final SecurityUtils securityUtils;
    private final DtoMapper mapper;

    @GetMapping("/orders")
    public List<VendorFulfillmentResponse> fulfillmentOrders() {
        return vendorService.getFulfillmentOrders(securityUtils.getApprovedVendor());
    }

    @PatchMapping("/orders/{id}/status")
    public VendorFulfillmentResponse updateOrderStatus(
            @PathVariable Long id,
            @Valid @RequestBody OrderStatusUpdateRequest request) {
        VendorOrder updated = orderService.updateVendorOrderStatus(
                securityUtils.getApprovedVendor(), id, request);
        return mapper.toFulfillmentResponse(updated);
    }

    @GetMapping("/analytics")
    public VendorAnalyticsResponse analytics() {
        return vendorService.getAnalytics(securityUtils.getApprovedVendor());
    }

    @GetMapping("/customers")
    public List<VendorCustomerInsightResponse> customers() {
        return vendorService.getCustomerInsights(securityUtils.getApprovedVendor());
    }

    @GetMapping("/products")
    public List<ProductResponse> myProducts() {
        return productService.getVendorProducts(securityUtils.getApprovedVendor());
    }

    @PostMapping("/categories")
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryResponse createCategory(@Valid @RequestBody CategoryRequest request) {
        return productService.createCategory(securityUtils.getApprovedVendor(), request);
    }

    @PostMapping("/products")
    @ResponseStatus(HttpStatus.CREATED)
    public ProductResponse createProduct(@Valid @RequestBody ProductRequest request) {
        return productService.createProduct(securityUtils.getApprovedVendor(), request);
    }

    @PutMapping("/products/{id}")
    public ProductResponse updateProduct(@PathVariable Long id, @Valid @RequestBody ProductRequest request) {
        return productService.updateProduct(securityUtils.getApprovedVendor(), id, request);
    }

    @DeleteMapping("/products/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(securityUtils.getApprovedVendor(), id);
    }

    @GetMapping("/profile")
    public VendorProfileResponse getProfile() {
        return vendorService.getProfile(securityUtils.getApprovedVendor());
    }

    @PutMapping("/profile")
    public VendorProfileResponse updateProfile(@Valid @RequestBody VendorProfileRequest request) {
        return vendorService.updateProfile(securityUtils.getApprovedVendor(), request);
    }

    @GetMapping("/earnings")
    public VendorEarningsResponse earnings() {
        return vendorService.getEarnings(securityUtils.getApprovedVendor());
    }
}
