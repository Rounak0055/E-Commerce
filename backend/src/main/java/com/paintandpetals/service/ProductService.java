package com.paintandpetals.service;

import com.paintandpetals.dto.request.CategoryRequest;
import com.paintandpetals.dto.request.ProductRequest;
import com.paintandpetals.dto.response.CategoryResponse;
import com.paintandpetals.dto.response.ProductResponse;
import com.paintandpetals.entity.Category;
import com.paintandpetals.entity.Product;
import com.paintandpetals.entity.User;
import com.paintandpetals.exception.ResourceNotFoundException;
import com.paintandpetals.mapper.DtoMapper;
import com.paintandpetals.repository.CategoryRepository;
import com.paintandpetals.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final DtoMapper mapper;

    public Page<ProductResponse> listProducts(String category, String search, String sort, int page, int size) {
        Pageable pageable = buildPageable(sort, page, size);

        Page<Product> products;
        if (search != null && !search.isBlank()) {
            products = productRepository.searchActive(search.trim(), pageable);
        } else if (category != null && !category.isBlank()) {
            products = productRepository.findByActiveTrueAndCategorySlug(category, pageable);
        } else {
            products = productRepository.findByActiveTrue(pageable);
        }

        return products.map(mapper::toProductResponse);
    }

    public ProductResponse getProduct(Long id) {
        Product product = productRepository.findById(id)
                .filter(Product::getActive)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        return mapper.toProductResponse(product);
    }

    @Transactional
    public CategoryResponse createCategory(User vendor, CategoryRequest request) {
        String normalizedName = request.getName().trim();
        String slug = normalizedName.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "-");
        String normalizedSlug = slug.replaceAll("(^-+|-+$)", "");

        Category category = categoryRepository.findBySlug(normalizedSlug)
                .orElseGet(() -> categoryRepository.save(Category.builder()
                        .name(normalizedName)
                        .slug(normalizedSlug)
                        .build()));

        return mapper.toCategoryResponse(category);
    }

    @Transactional
    public ProductResponse createProduct(User vendor, ProductRequest request) {
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        Product product = Product.builder()
                .vendor(vendor)
                .category(category)
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .compareAtPrice(request.getCompareAtPrice())
                .stockQuantity(request.getStockQuantity())
                .shippingTerms(request.getShippingTerms())
                .imageUrl(request.getImageUrl())
                .active(request.getActive() != null ? request.getActive() : true)
                .build();

        return mapper.toProductResponse(productRepository.save(product));
    }

    @Transactional
    public ProductResponse updateProduct(User vendor, Long id, ProductRequest request) {
        Product product = getVendorProduct(vendor, id);
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setCompareAtPrice(request.getCompareAtPrice());
        product.setStockQuantity(request.getStockQuantity());
        product.setShippingTerms(request.getShippingTerms());
        product.setImageUrl(request.getImageUrl());
        product.setActive(request.getActive() != null ? request.getActive() : product.getActive());
        product.setCategory(category);

        return mapper.toProductResponse(productRepository.save(product));
    }

    @Transactional
    public void deleteProduct(User vendor, Long id) {
        Product product = getVendorProduct(vendor, id);
        product.setActive(false);
        productRepository.save(product);
    }

    public List<ProductResponse> getVendorProducts(User vendor) {
        List<ProductResponse> responses = new ArrayList<>();
        for (Product product : productRepository.findByVendor(vendor)) {
            responses.add(mapper.toProductResponse(product));
        }
        return responses;
    }

    private Product getVendorProduct(User vendor, Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        if (!product.getVendor().getId().equals(vendor.getId())) {
            throw new ResourceNotFoundException("Product not found");
        }
        return product;
    }

    private Pageable buildPageable(String sort, int page, int size) {
        Sort sorting = Sort.by(Sort.Direction.DESC, "createdAt");
        if ("price_asc".equals(sort)) {
            sorting = Sort.by(Sort.Direction.ASC, "price");
        } else if ("price_desc".equals(sort)) {
            sorting = Sort.by(Sort.Direction.DESC, "price");
        } else if ("name".equals(sort)) {
            sorting = Sort.by(Sort.Direction.ASC, "name");
        }
        return PageRequest.of(page, size, sorting);
    }
}
