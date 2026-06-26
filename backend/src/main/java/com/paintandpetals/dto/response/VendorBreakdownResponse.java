package com.paintandpetals.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import com.paintandpetals.entity.enums.VendorStatus;

@Data
@Builder
public class VendorBreakdownResponse {
    private Long vendorId;
    private String vendorName;
    private String email;
    private VendorStatus vendorStatus;
    private long activeOrders;
    private BigDecimal totalRevenue;
    private BigDecimal averageOrderValue;
}
