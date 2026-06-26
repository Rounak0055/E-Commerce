package com.paintandpetals.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class AdminDashboardResponse {
    private BigDecimal totalRevenue;
    private BigDecimal averageOrderValue;
    private long activeVendorCount;
    private long dailyOrderCount;
    private BigDecimal dailyRevenue;
    private List<VendorBreakdownResponse> vendorBreakdown;
    private List<CustomerDirectoryResponse> customers;
}
