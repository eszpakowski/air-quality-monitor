package com.interview.monitor.adapters.outbound.db;

import com.interview.monitor.domain.model.Measurement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.jdbc.JdbcTestUtils;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.interview.monitor.testutils.TestConstants.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ActiveProfiles("test")
class DuckDbMeasurementRepositoryIT {
    private static final String TABLE_NAME = "measurements";

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    DuckDbMeasurementRepository underTest;

    @BeforeEach
    void clearTable() {
        JdbcTestUtils.deleteFromTables(jdbcTemplate, TABLE_NAME);
    }

    @Test
    void shouldCreateExpectedEntities() {
        // given
        Measurement measurement1 = createValidMeasurement();
        Measurement measurement2 = createValidMeasurement();
        Measurement measurement3 = createValidMeasurement();

        Integer countBefore = countMeasurements();

        // when
        underTest.save(measurement1);
        underTest.save(measurement2);
        underTest.save(measurement3);

        // then
        Integer countAfter = countMeasurements();
        assertEquals(countBefore + 3, countAfter);
    }

    @Test
    void shouldSaveAll() {
        // given
        Measurement measurement1 = createValidMeasurement();
        Measurement measurement2 = createValidMeasurement();
        Measurement measurement3 = createValidMeasurement();

        Integer countBefore = countMeasurements();

        // when
        underTest.saveAll(List.of(measurement1, measurement2, measurement3));

        // then
        Integer countAfter = countMeasurements();
        assertEquals(countBefore + 3, countAfter);
    }

    @Test
    void queryRisingCities_shouldReturnEmpty_whenNoRisingTendenciesInMeasurements() {
        // given
        var measurements = new ArrayList<Measurement>();
        measurements.addAll(generateTestMeasurements(SIEDLCE_CITY_ID, new BigDecimal("23.1"), new BigDecimal("7.0"), new BigDecimal("0.34"), 6));
        measurements.addAll(generateTestMeasurements(RADOM_CITY_ID, new BigDecimal("13.1"), new BigDecimal("12.4"), new BigDecimal("0.19"), 6));
        measurements.addAll(generateTestMeasurements(PLOCK_CITY_ID, new BigDecimal("33.1"), new BigDecimal("22.4"), new BigDecimal("4.39"), 6));
        measurements.addAll(generateTestMeasurements(WARSZAWA_CITY_ID, new BigDecimal("23.1"), new BigDecimal("12.4"), new BigDecimal("0.39"), 6));

        underTest.saveAll(measurements);

        // when
        List<String> actual1 = underTest.queryRisingCO5MCities(MAZOWIECKIE_REGION_ID);
        List<String> actual2 = underTest.queryRisingCO5MCities(MAZOWIECKIE_REGION_ID);

        // then
        assertThat(actual1).isEmpty();
        assertThat(actual2).isEmpty();
    }

    @Test
    void queryRisingCities_shouldReturnCityName_whenRisingTendenciesInMeasurements() {
        // given
        var measurements = new ArrayList<Measurement>();
        measurements.addAll(generateTestMeasurements(SIEDLCE_CITY_ID, new BigDecimal("23.1"), new BigDecimal("7.0"), new BigDecimal("0.34"), 6));
        measurements.addAll(generateTestMeasurements(RADOM_CITY_ID, new BigDecimal("13.1"), new BigDecimal("12.4"), new BigDecimal("0.19"), 6));
        measurements.addAll(generateTestMeasurements(PLOCK_CITY_ID, new BigDecimal("33.1"), new BigDecimal("22.4"), new BigDecimal("4.39"), 6));
        // rising each month
        measurements.addAll(generateTestMeasurements(WARSZAWA_CITY_ID, new BigDecimal("23.1"), new BigDecimal("12.4"), new BigDecimal("0.39"), 6, true));

        underTest.saveAll(measurements);

        // when
        List<String> actual1 = underTest.queryRisingCO5MCities(MAZOWIECKIE_REGION_ID);
        List<String> actual2 = underTest.queryRisingCO5MCities(MAZOWIECKIE_REGION_ID);

        // then
        assertThat(actual1).contains("Warszawa");
        assertThat(actual2).contains("Warszawa");
    }


    private Integer countMeasurements() {
        return jdbcTemplate.queryForObject("SELECT count(*) from measurements", Integer.class);
    }

    private static Measurement createValidMeasurement() {
        return new Measurement(null, SENSOR_ID, SIEDLCE_CITY_ID, PM_10, CO, NO_2, Instant.now());
    }

    private static List<Measurement> generateTestMeasurements(UUID cityId, BigDecimal pm10, BigDecimal co, BigDecimal no2, int months) {
        return generateTestMeasurements(cityId, pm10, co, no2, months, false);
    }

    private static List<Measurement> generateTestMeasurements(UUID cityId, BigDecimal pm10, BigDecimal co, BigDecimal no2, int months, boolean isRising) {
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