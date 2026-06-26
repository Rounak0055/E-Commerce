package com.paintandpetals.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class VendorEarningsResponse {
    private BigDecimal settledRevenue;
    private BigDecimal pendingPayout;
    private BigDecimal availableBalance;
}
