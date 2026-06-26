package com.paintandpetals.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class VendorProfileRequest {
    @NotBlank
    private String businessName;

    private String description;

    private String bannerUrl;

    private String logoUrl;

    private String bio;
}
