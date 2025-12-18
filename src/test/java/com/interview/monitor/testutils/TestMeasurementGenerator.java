package com.interview.monitor.testutils;

import com.interview.monitor.domain.model.Measurement;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TestMeasurementGenerator {
    private TestMeasurementGenerator() {}

    public static List<Measurement> generateTestMeasurements(UUID cityId, BigDecimal pm10, BigDecimal co, BigDecimal no2, int months) {
        return generateTestMeasurements(cityId, pm10, co, no2, months, false);
    }

    public static List<Measurement> generateTestMeasurements(UUID cityId, BigDecimal pm10, BigDecimal co, BigDecimal no2, int months, boolean isRising) {
        var measurements = new ArrayList<Measurement>();
        LocalDate end = LocalDate.now();
        LocalDate next = LocalDate.now().minusMonths(months);
        while ((next = next.plusDays(1)).isBefore(end)) {
            if (isRising) {
                pm10 = pm10.add(new BigDecimal("1.0"));
                co = co.add(new BigDecimal("1.0"));
                no2 = no2.add(new BigDecimal("1.0"));
            }
            measurements.add(new Measurement(null, UUID.randomUUID(), cityId, pm10, co, no2, next.atStartOfDay().toInstant(ZoneOffset.UTC)));
            measurements.add(new Measurement(null, UUID.randomUUID(), cityId, pm10, co, no2, next.atStartOfDay().plusHours(8).toInstant(ZoneOffset.UTC)));
        }
        return measurements;
    }
}
