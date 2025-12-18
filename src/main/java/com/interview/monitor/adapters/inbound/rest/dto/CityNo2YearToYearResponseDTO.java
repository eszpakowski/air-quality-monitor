package com.interview.monitor.adapters.inbound.rest.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record CityNo2YearToYearResponseDTO(
        String city,
        UUID cityId,
        String country,
        BigDecimal avgNo2Current,
        BigDecimal avgNo2YearBefore
) {
}