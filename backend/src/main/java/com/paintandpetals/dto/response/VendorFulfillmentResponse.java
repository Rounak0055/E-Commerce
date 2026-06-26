package com.paintandpetals.dto.response;

import com.paintandpetals.entity.enums.OrderStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
@Builder
public class VendorFulfillmentResponse {
    private Long vendorOrderId;
    private Long orderGroupId;
    private OrderStatus status;
    private Instant orderDate;
    private String customerName;
    private String customerEmail;
    private String shippingStreet;
    private String shippingCity;
    private String shippingState;
    private String shippingZip;
    private String shippingCountry;
    private String shippingPhone;
    private BigDecimal subtotal;
    private List<FulfillmentItemResponse> items;
}
