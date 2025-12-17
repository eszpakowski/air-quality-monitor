package com.interview.monitor.adapters.outbound.db;

import com.interview.monitor.domain.model.Measurement;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;

import static com.interview.monitor.testutils.TestConstants.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ActiveProfiles("test")
class DuckDbMeasurementRepositoryIT {
    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    DuckDbMeasurementRepository underTest;

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

    private Integer countMeasurements() {
        return jdbcTemplate.queryForObject("SELECT count(*) from measurements", Integer.class);
    }

    private static Measurement createValidMeasurement() {
        return new Measurement(null, SENSOR_ID, WARSAW_CITY_ID, PM_10, CO, NO_2, Instant.now());
    }
}