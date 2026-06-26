package com.paintandpetals.dto.response;

import com.paintandpetals.entity.enums.OrderStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class VendorOrderResponse {
    private Long id;
    private Long vendorId;
    private String vendorName;
    private OrderStatus status;
    private BigDecimal subtotal;
    private List<OrderItemResponse> items;
}
