package com.interview.monitor.adapters.outbound.db;

import com.interview.monitor.adapters.inbound.rest.dto.CityStatsResponseDTO;
import com.interview.monitor.domain.model.Measurement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.jdbc.JdbcTestUtils;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.interview.monitor.testutils.TestConstants.*;
import static com.interview.monitor.testutils.TestMeasurementGenerator.generateTestMeasurements;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class DuckDbMeasurementRepositoryIT {
    private static final String TABLE_NAME = "measurements";
    public static final String WORST_CITIES_HEADER = "CITY,REGION,PM10";

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
    void queryRisingCities_shouldReturnEmpty_whenUnexistingRegionIdIsUsed() {
        // given
        var measurements = new ArrayList<Measurement>();
        measurements.addAll(generateTestMeasurements(SIEDLCE_CITY_ID, new BigDecimal("23.1"), new BigDecimal("7.0"), new BigDecimal("0.34"), 6));
        measurements.addAll(generateTestMeasurements(RADOM_CITY_ID, new BigDecimal("13.1"), new BigDecimal("12.4"), new BigDecimal("0.19"), 6));
        measurements.addAll(generateTestMeasurements(PLOCK_CITY_ID, new BigDecimal("33.1"), new BigDecimal("22.4"), new BigDecimal("4.39"), 6));
        measurements.addAll(generateTestMeasurements(WARSZAWA_CITY_ID, new BigDecimal("23.1"), new BigDecimal("12.4"), new BigDecimal("0.39"), 6));

        underTest.saveAll(measurements);

        UUID unexistingRegionId = UUID.randomUUID();

        // when
        List<String> actual1 = underTest.queryRisingCO5MCities(unexistingRegionId);
        List<String> actual2 = underTest.queryRisingCO5MCities(unexistingRegionId);

        // then
        assertThat(actual1).isEmpty();
        assertThat(actual2).isEmpty();
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

    @Test
    void queryCityStatsLastHour_shouldNotReturnStatsWhenNoMeasurementPresentInTheLastHour() {
        // given
        var twoHoursAgo = Instant.now().minus(2, ChronoUnit.HOURS);
        var measurement1 = createMeasurement(WARSZAWA_CITY_ID, "10.00", "10.00", "10.00", twoHoursAgo);
        var measurement2 = createMeasurement(WARSZAWA_CITY_ID, "50.00", "50.00", "50.00", twoHoursAgo);
        var measurement3 = createMeasurement(WARSZAWA_CITY_ID, "99.99", "99.99", "99.99", twoHoursAgo);
        underTest.saveAll(List.of(measurement1, measurement2, measurement3));

        // when
        Optional<CityStatsResponseDTO> actual = underTest.queryCityStatsLastHour(WARSZAWA_CITY_ID);

        // then
        assertThat(actual).isEmpty();
    }

    @Test
    void queryCityStatsLastHour_shouldReturnExpectedCityStats() {
        // given
        var measurement1 = createMeasurement(WARSZAWA_CITY_ID, "10.00", "10.00", "10.00", Instant.now());
        var measurement2 = createMeasurement(WARSZAWA_CITY_ID, "50.00", "50.00", "50.00", Instant.now().plusSeconds(60));
        var measurement3 = createMeasurement(WARSZAWA_CITY_ID, "99.99", "99.99", "99.99", Instant.now().plusSeconds(120));
        underTest.saveAll(List.of(measurement1, measurement2, measurement3));

        // when
        Optional<CityStatsResponseDTO> actual = underTest.queryCityStatsLastHour(WARSZAWA_CITY_ID);

        // then
        var expected = new CityStatsResponseDTO(
                new BigDecimal("53.33"), new BigDecimal("99.99"), new BigDecimal("10.00"),
                new BigDecimal("53.33"), new BigDecimal("99.99"), new BigDecimal("10.00"),
                new BigDecimal("53.33"), new BigDecimal("99.99"), new BigDecimal("10.00"));
        assertThat(actual).hasValue(expected);
    }

    @Test
    void generateMonthlyHighestPM10Report_shouldGenerateEmptyFile_whenDataForLastMonth(@TempDir Path tempDir) {
        // given
        Path report = tempDir.resolve("temp_file.csv");

        var twoHoursAgo = Instant.now().minus(2, ChronoUnit.HOURS);
        var measurement1 = createMeasurement(WARSZAWA_CITY_ID, "10.00", "10.00", "10.00", twoHoursAgo);
        var measurement2 = createMeasurement(WARSZAWA_CITY_ID, "50.00", "50.00", "50.00", twoHoursAgo);
        var measurement3 = createMeasurement(WARSZAWA_CITY_ID, "99.99", "99.99", "99.99", twoHoursAgo);
        underTest.saveAll(List.of(measurement1, measurement2, measurement3));

        // when
        underTest.generateMonthlyHighestPM10Report(report.toAbsolutePath().toString());

        // then
        List<String> expectedLines = List.of(WORST_CITIES_HEADER);
        assertAll(
                () -> assertTrue(Files.exists(report)),
                () -> assertLinesMatch(expectedLines, Files.readAllLines(report)));
    }

    @Test
    void generateMonthlyHighestPM10Report_shouldGenerateExpectedFile(@TempDir Path tempDir) {
        // given
        Path report = tempDir.resolve("temp_file.csv");

        var measurements = new ArrayList<Measurement>();
        measurements.addAll(generateTestMeasurements(SIEDLCE_CITY_ID, new BigDecimal("23.1"), new BigDecimal("7.0"), new BigDecimal("0.34"), 2));
        measurements.addAll(generateTestMeasurements(RADOM_CITY_ID, new BigDecimal("13.1"), new BigDecimal("12.4"), new BigDecimal("0.19"), 2));
        measurements.addAll(generateTestMeasurements(PLOCK_CITY_ID, new BigDecimal("33.1"), new BigDecimal("22.4"), new BigDecimal("4.39"), 2));
        measurements.addAll(generateTestMeasurements(WARSZAWA_CITY_ID, new BigDecimal("23.1"), new BigDecimal("12.4"), new BigDecimal("0.39"), 2));

        underTest.saveAll(measurements);

        // when
        underTest.generateMonthlyHighestPM10Report(report.toAbsolutePath().toString());

        // then
        List<String> expectedLines = List.of(
                WORST_CITIES_HEADER,
                "Płock,Mazowieckie,33.1",
                "Siedlce,Mazowieckie,23.1",
                "Warszawa,Mazowieckie,23.1",
                "Radom,Mazowieckie,13.1"
        );
        assertAll(
                () -> assertTrue(Files.exists(report)),
                () -> assertLinesMatch(
                        expectedLines.stream().sorted(),
                        Files.readAllLines(report).stream().sorted())
        );
    }


    private Integer countMeasurements() {
        return jdbcTemplate.queryForObject("SELECT count(*) from measurements", Integer.class);
    }

    private static Measurement createValidMeasurement() {
        return createMeasurement(SIEDLCE_CITY_ID, "23.1", "12.4", "0.39", Instant.now());
    }

    private static Measurement createMeasurement(UUID cityId, String pm10, String co, String no2, Instant timestamp) {
        return new Measurement(null, UUID.randomUUID(), cityId, new BigDecimal(pm10), new BigDecimal(co), new BigDecimal(no2), timestamp);
    }


}