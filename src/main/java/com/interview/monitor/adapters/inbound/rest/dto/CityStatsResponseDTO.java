package com.interview.monitor.adapters.inbound.rest.dto;

import java.math.BigDecimal;

public record CityStatsResponseDTO(
        BigDecimal avgNO2LastHour,
        BigDecimal maxNO2LastHour,
        BigDecimal minNO2LastHour,
        BigDecimal avgCOLastHour,
        BigDecimal maxCOLastHour,
        BigDecimal minCOLastHour,
        BigDecimal avgPM10LastHour,
        BigDecimal maxPM10LastHour,
        BigDecimal minPM10LastHour
) {
}