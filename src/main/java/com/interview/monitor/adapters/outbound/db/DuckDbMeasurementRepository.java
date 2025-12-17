package com.interview.monitor.adapters.outbound.db;

import com.interview.monitor.domain.exception.DatastoreException;
import com.interview.monitor.domain.model.Measurement;
import com.interview.monitor.domain.ports.outbound.MeasurementRepository;
import org.duckdb.DuckDBConnection;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

@Repository
public class DuckDbMeasurementRepository implements MeasurementRepository {
    private static final String INSERT_MEASUREMENT_SQL = """
            INSERT INTO measurements (id, sensor_id, city_id, pm10, co, no2, timestamp)
            VALUES (nextval('measurements_seq'), ?, ?, ?, ?, ?, ?)
            """;

    private final String datasourceUrl;

    public DuckDbMeasurementRepository(@Value("${spring.datasource.url}")String datasourceUrl) {
        this.datasourceUrl = datasourceUrl;
    }

    @Override
    public void save(Measurement measurement) {
        try (var conn = (DuckDBConnection) DriverManager.getConnection(datasourceUrl);
             PreparedStatement stmt = conn.prepareStatement(INSERT_MEASUREMENT_SQL)) {
            stmt.setObject(1, measurement.sensorId());
            stmt.setObject(2, measurement.cityId());
            stmt.setBigDecimal(3, measurement.pm10());
            stmt.setBigDecimal(4, measurement.co());
            stmt.setBigDecimal(5, measurement.no2());
            stmt.setTimestamp(6, Timestamp.from(measurement.timestamp()));

            stmt.execute();
        } catch (SQLException ex) {
            throw new DatastoreException("Problems occurred when acquiring connection", ex);
        }
    }
}
