package com.paintandpetals.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class ProductResponse {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer stockQuantity;
    private String imageUrl;
    private Long categoryId;
    private String categoryName;
    private String categorySlug;
    private Long vendorId;
    private String vendorName;
    private java.math.BigDecimal compareAtPrice;
    private String shippingTerms;
    private Boolean active;
}
