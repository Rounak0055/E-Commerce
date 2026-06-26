package com.paintandpetals.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class VendorAnalyticsResponse {
    private BigDecimal dailySales;
    private BigDecimal weeklySales;
    private BigDecimal monthlySales;
    private List<SalesDataPoint> salesTrend;
    private long pendingCount;
    private long shippedCount;
    private long cancelledCount;
    private long completedCount;
    private List<String> outOfStockProducts;
}
