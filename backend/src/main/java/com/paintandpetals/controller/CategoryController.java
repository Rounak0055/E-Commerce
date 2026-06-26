package com.paintandpetals.controller;

import com.paintandpetals.dto.request.CategoryRequest;
import com.paintandpetals.dto.response.CategoryResponse;
import com.paintandpetals.entity.Category;
import com.paintandpetals.mapper.DtoMapper;
import com.paintandpetals.repository.CategoryRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryRepository categoryRepository;
    private final DtoMapper mapper;

    @GetMapping
    public List<CategoryResponse> listCategories() {
        return categoryRepository.findAll().stream()
                .map(mapper::toCategoryResponse)
                .collect(Collectors.toList());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryResponse createCategory(@Valid @RequestBody CategoryRequest request) {
        String normalizedName = request.getName().trim();
        String slug = createUniqueSlug(normalizedName);

        Category category = categoryRepository.save(Category.builder()
                .name(normalizedName)
                .slug(slug)
                .build());

        return mapper.toCategoryResponse(category);
    }

    private String createUniqueSlug(String name) {
        String baseSlug = name.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "-");
        baseSlug = baseSlug.replaceAll("(^-+|-+$)", "");
        if (baseSlug.isBlank()) {
            baseSlug = "category";
        }

        String slug = baseSlug;
        int suffix = 2;
        while (categoryRepository.findBySlug(slug).isPresent()) {
            slug = baseSlug + "-" + suffix;
            suffix++;
        }
        return slug;
    }
}
