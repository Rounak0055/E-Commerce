package com.paintandpetals.dto.response;

import com.paintandpetals.entity.enums.OrderStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
@Builder
public class OrderGroupResponse {
    private Long id;
    private OrderStatus status;
    private BigDecimal totalAmount;
    private String shippingStreet;
    private String shippingCity;
    private String shippingState;
    private String shippingZip;
    private String shippingCountry;
    private String shippingPhone;
    private Instant createdAt;
    private List<VendorOrderResponse> vendorOrders;
    private PaymentInitResponse payment;
}
