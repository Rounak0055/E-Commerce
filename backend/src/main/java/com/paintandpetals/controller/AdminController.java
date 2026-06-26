package com.paintandpetals.controller;

import com.paintandpetals.dto.request.OrderStatusUpdateRequest;
import com.paintandpetals.dto.request.CategoryRequest;
import com.paintandpetals.dto.request.MarketplaceSettingsRequest;
import com.paintandpetals.dto.response.AdminDashboardResponse;
import com.paintandpetals.dto.response.CategoryResponse;
import com.paintandpetals.dto.response.CustomerDirectoryResponse;
import com.paintandpetals.dto.response.MarketplaceSettingsResponse;
import com.paintandpetals.dto.response.OrderGroupResponse;
import com.paintandpetals.dto.response.UserResponse;
import com.paintandpetals.entity.Category;
import com.paintandpetals.entity.OrderGroup;
import com.paintandpetals.exception.BadRequestException;
import com.paintandpetals.exception.ResourceNotFoundException;
import com.paintandpetals.mapper.DtoMapper;
import com.paintandpetals.repository.CategoryRepository;
import com.paintandpetals.repository.ProductRepository;
import com.paintandpetals.service.AdminService;
import com.paintandpetals.service.OrderService;
import org.springframework.http.HttpStatus;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final OrderService orderService;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final DtoMapper mapper;

    @GetMapping("/dashboard")
    public AdminDashboardResponse dashboard() {
        return adminService.getDashboard();
    }

    @GetMapping("/vendors/pending")
    public List<UserResponse> pendingVendors() {
        return adminService.getPendingVendors();
    }

    @GetMapping("/vendors")
    public List<UserResponse> vendors() {
        return adminService.getVendors();
    }

    @PostMapping("/vendors/{id}/approve")
    public UserResponse approveVendor(@PathVariable Long id) {
        return adminService.approveVendor(id);
    }

    @PostMapping("/vendors/{id}/reject")
    public UserResponse rejectVendor(@PathVariable Long id) {
        return adminService.rejectVendor(id);
    }

    @GetMapping("/customers")
    public List<CustomerDirectoryResponse> customers() {
        return adminService.getCustomerDirectory();
    }

    @GetMapping("/settings")
    public MarketplaceSettingsResponse settings() {
        return adminService.getMarketplaceSettings();
    }

    @PutMapping("/settings")
    public MarketplaceSettingsResponse updateSettings(@Valid @RequestBody MarketplaceSettingsRequest request) {
        return adminService.updateMarketplaceSettings(request);
    }

    @GetMapping("/categories")
    public List<CategoryResponse> categories() {
        return categoryRepository.findAll().stream()
                .map(mapper::toCategoryResponse)
                .collect(Collectors.toList());
    }

    @PutMapping("/categories/{id}")
    public CategoryResponse updateCategory(@PathVariable Long id, @Valid @RequestBody CategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        String normalizedName = request.getName().trim();

        categoryRepository.findByNameIgnoreCase(normalizedName)
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new BadRequestException("Category name already exists");
                });

        category.setName(normalizedName);
        category.setSlug(createUniqueSlug(normalizedName, id));
        return mapper.toCategoryResponse(categoryRepository.save(category));
    }

    @DeleteMapping("/categories/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCategory(@PathVariable Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        if (productRepository.countByCategory(category) > 0) {
            throw new BadRequestException("Only empty categories can be deleted");
        }
        categoryRepository.delete(category);
    }

    @DeleteMapping("/users/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable Long id) {
        adminService.deleteUser(id);
    }

    @PatchMapping("/orders/{id}/status")
    public OrderGroupResponse updateOrderStatus(
            @PathVariable Long id,
            @Valid @RequestBody OrderStatusUpdateRequest request) {
        OrderGroup updated = orderService.updateOrderGroupStatus(null, id, request);
        return mapper.toOrderGroupResponse(updated);
    }

    private String createUniqueSlug(String name, Long categoryId) {
        String baseSlug = name.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "-");
        baseSlug = baseSlug.replaceAll("(^-+|-+$)", "");
        if (baseSlug.isBlank()) {
            baseSlug = "category";
        }

        String slug = baseSlug;
        int suffix = 2;
        while (categoryRepository.findBySlug(slug)
                .filter(existing -> !existing.getId().equals(categoryId))
                .isPresent()) {
            slug = baseSlug + "-" + suffix;
            suffix++;
        }
        return slug;
    }
}
