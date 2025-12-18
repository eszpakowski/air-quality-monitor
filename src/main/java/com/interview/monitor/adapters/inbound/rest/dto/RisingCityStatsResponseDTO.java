package com.interview.monitor.adapters.inbound.rest.dto;

import java.util.List;

public record RisingCityStatsResponseDTO(
        List<String> risingCO5MCities,
        List<String> risingPM105MCities
) {
}
