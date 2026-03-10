package com.revshop.product.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public class ThresholdRequest {

    @NotNull(message = "Threshold value is required")
    @PositiveOrZero(message = "Threshold must be zero or positive")
    private Integer threshold;

    public ThresholdRequest() {
    }

    public ThresholdRequest(Integer threshold) {
        this.threshold = threshold;
    }

    public Integer getThreshold() { return threshold; }
    public void setThreshold(Integer threshold) { this.threshold = threshold; }
}
