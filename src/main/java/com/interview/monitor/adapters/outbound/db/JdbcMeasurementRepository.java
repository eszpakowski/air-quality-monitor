package com.interview.monitor.adapters.outbound.db;

import com.interview.monitor.domain.model.Measurement;
import com.interview.monitor.domain.ports.outbound.MeasurementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;

@Repository
@RequiredArgsConstructor
public class JdbcMeasurementRepository implements MeasurementRepository {
    private static final String INSERT_MEASUREMENT_SQL = """
            INSERT INTO measurements (id, sensor_id, city_id, pm10, co, no2, timestamp)
            VALUES (nextval('measurements_seq'), ?, ?, ?, ?, ?, ?)
            """;

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void save(Measurement measurement) {
        jdbcTemplate.update(
                INSERT_MEASUREMENT_SQL,
                measurement.sensorId(),
                measurement.cityId(),
                measurement.pm10(),
                measurement.co(),
                measurement.no2(),
                Timestamp.from(measurement.timestamp())
        );
    }
}
