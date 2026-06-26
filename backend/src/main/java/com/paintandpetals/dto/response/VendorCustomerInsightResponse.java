package com.paintandpetals.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class VendorCustomerInsightResponse {
    private Long customerId;
    private String name;
    private String email;
    private String phone;
    private BigDecimal totalSpent;
    private long orderCount;
}
