package com.paintandpetals.service;

import com.paintandpetals.dto.request.CategoryRequest;
import com.paintandpetals.dto.response.CategoryResponse;
import com.paintandpetals.entity.Category;
import com.paintandpetals.entity.User;
import com.paintandpetals.mapper.DtoMapper;
import com.paintandpetals.repository.CategoryRepository;
import com.paintandpetals.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private DtoMapper mapper;

    @InjectMocks
    private ProductService productService;

    @Test
    void createCategoryPersistsNormalizedSlugAndReturnsResponse() {
        CategoryRequest request = new CategoryRequest();
        request.setName("Handmade Bouquets");

        Category savedCategory = Category.builder()
                .id(7L)
                .name("Handmade Bouquets")
                .slug("handmade-bouquets")
                .build();

        when(categoryRepository.findBySlug("handmade-bouquets")).thenReturn(Optional.empty());
        when(categoryRepository.save(any(Category.class))).thenReturn(savedCategory);
        when(mapper.toCategoryResponse(savedCategory)).thenReturn(CategoryResponse.builder()
                .id(7L)
                .name("Handmade Bouquets")
                .slug("handmade-bouquets")
                .build());

        CategoryResponse response = productService.createCategory(User.builder().id(1L).build(), request);

        assertThat(response.getSlug()).isEqualTo("handmade-bouquets");
        verify(categoryRepository).save(any(Category.class));
    }
}
