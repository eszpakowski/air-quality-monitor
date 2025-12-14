package com.interview.monitor.domain.model;

import com.interview.monitor.adapters.inbound.rest.dto.MeasurementRequestDTO;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.util.UUID;

public record Measurement(
        BigInteger id,
        UUID sensorId,
        UUID cityId,
        BigDecimal pm10,
        BigDecimal co,
        BigDecimal no2,
        Instant timestamp
) {
    public static Measurement fromRequest(MeasurementRequestDTO request) {
        return new Measurement(
                null,
                UUID.fromString(request.sensorId()),
                UUID.fromString(request.cityId()),
                request.pm10(),
                request.co(),
                request.no2(),
                Instant.ofEpochSecond(request.timestamp())
        );
    }
}