package com.paintandpetals.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class VendorProfileResponse {
    private Long id;
    private String businessName;
    private String description;
    private String bannerUrl;
    private String logoUrl;
    private String bio;
}
