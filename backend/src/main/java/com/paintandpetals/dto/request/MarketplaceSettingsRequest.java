package com.paintandpetals.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.DecimalMax;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class MarketplaceSettingsRequest {
    @DecimalMin("0.00")
    @DecimalMax("100.00")
    private BigDecimal commissionRate;

    private Boolean alertBannerEnabled;

    private String alertBannerMessage;
}
