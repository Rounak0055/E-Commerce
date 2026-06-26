package com.paintandpetals.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class SalesDataPoint {
    private String date;
    private BigDecimal amount;
}
