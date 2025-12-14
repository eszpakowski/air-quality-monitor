package com.interview.monitor.adapters.inbound.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record MeasurementRequestDTO(
        @NotBlank(message = "sensorId is required")
        String sensorId,

        @NotBlank(message = "cityId is required")
        String cityId,

        @NotNull(message = "PM10 is required")
        BigDecimal pm10,

        @NotNull(message = "CO is required")
        BigDecimal co,

        @NotNull(message = "NO2 is required")
        BigDecimal no2,

        @NotNull(message = "timestamp is required")
        Long timestamp
) {
}
