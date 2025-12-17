package com.interview.monitor.adapters.outbound.rest.dto;

import java.util.UUID;

public record CityInfoDTO(
        String country,
        String city,
        UUID cityId,
        String region,
        UUID regionId
) {
}