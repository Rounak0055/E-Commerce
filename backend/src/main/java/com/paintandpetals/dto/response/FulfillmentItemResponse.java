package com.paintandpetals.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class FulfillmentItemResponse {
    private String productName;
    private Integer quantity;
    private BigDecimal unitPrice;
}
