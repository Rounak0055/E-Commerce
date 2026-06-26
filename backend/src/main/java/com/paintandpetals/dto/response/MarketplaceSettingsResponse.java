package com.paintandpetals.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class MarketplaceSettingsResponse {
    private BigDecimal commissionRate;
    private boolean alertBannerEnabled;
    private String alertBannerMessage;
}
