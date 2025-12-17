package com.interview.monitor.domain.model;

import java.util.UUID;

public record City(
        UUID id,
        String name,
        String country,
        String region,
        UUID regionId
) {
}
