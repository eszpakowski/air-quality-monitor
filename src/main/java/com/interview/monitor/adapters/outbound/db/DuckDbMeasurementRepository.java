package com.interview.monitor.adapters.outbound.db;

import com.interview.monitor.adapters.inbound.rest.dto.CityStatsResponseDTO;
import com.interview.monitor.domain.exception.DatastoreException;
import com.interview.monitor.domain.model.Measurement;
import com.interview.monitor.domain.ports.outbound.MeasurementRepository;
import org.duckdb.DuckDBConnection;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class DuckDbMeasurementRepository implements MeasurementRepository {
    private static final String INSERT_MEASUREMENT_SQL = """
            INSERT INTO measurements (id, sensor_id, city_id, pm10, co, no2, timestamp)
            VALUES (nextval('measurements_seq'), ?, ?, ?, ?, ?, ?)
            """;

    private static final String COLUMN_NAME_CO = "co";
    private static final String COLUMN_NAME_PM10 = "pm10";
    private static final String RISING_REGION_AVG_5MONTH_SQL = """
            WITH monthly_co_avg AS (
                SELECT
                    c.name,
                    m.city_id,
                    time_bucket(INTERVAL '1 month', m.timestamp) AS month,
                    AVG(m.%s) AS avg_val
                FROM measurements m
                JOIN cities c ON c.id = m.city_id
                WHERE c.region_id = ?
                  AND m.timestamp < DATE_TRUNC('month', CURRENT_DATE)
                  AND m.timestamp >= DATE_TRUNC('month', CURRENT_DATE) - INTERVAL '5 months'
                GROUP BY c.name, m.city_id, time_bucket(INTERVAL '1 month', m.timestamp)
            ),
            with_trend AS (
                SELECT
                    name,
                    city_id,
                    avg_val > LAG(avg_val) OVER (PARTITION BY city_id ORDER BY month) AS is_rising
                FROM monthly_co_avg
            )
            SELECT name
            FROM with_trend
            GROUP BY name
            HAVING COUNT(*) = 5 AND COUNT(*) FILTER (WHERE is_rising) = 4;
            """;
    private static final String CITY_STATS_LAST_HOUR_SQL = """
            SELECT
                MIN(no2) as min_no2,
                AVG(no2) as avg_no2,
                MAX(no2) as max_no2,
                MIN(co) as min_co,
                AVG(co) as avg_co,
                MAX(co) as max_co,
                MIN(pm10) as min_pm10,
                AVG(pm10) as avg_pm10,
                MAX(pm10) as max_pm10,
            FROM measurements m
            WHERE m.city_id = ?
              AND m.timestamp >= NOW() - INTERVAL '1 hour';
            """;

    private final String datasourceUrl;

    public DuckDbMeasurementRepository(@Value("${spring.datasource.url}") String datasourceUrl) {
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

    @Override
    public void saveAll(List<Measurement> measurements) {
        try (var conn = (DuckDBConnection) DriverManager.getConnection(datasourceUrl);
             PreparedStatement stmt = conn.prepareStatement(INSERT_MEASUREMENT_SQL)) {
            for (Measurement measurement : measurements) {
                stmt.setObject(1, measurement.sensorId());
                stmt.setObject(2, measurement.cityId());
                stmt.setBigDecimal(3, measurement.pm10());
                stmt.setBigDecimal(4, measurement.co());
                stmt.setBigDecimal(5, measurement.no2());
                stmt.setTimestamp(6, Timestamp.from(measurement.timestamp()));

                stmt.addBatch();
            }

            stmt.executeBatch();
        } catch (SQLException ex) {
            throw new DatastoreException("Problems occurred when accessing database", ex);
        }
    }

    @Override
    public List<String> queryRisingCO5MCities(UUID regionId) {
        return queryRisingCities(RISING_REGION_AVG_5MONTH_SQL.formatted(COLUMN_NAME_CO), regionId);
    }

    @Override
    public List<String> queryRisingPM105MCities(UUID regionId) {
        return queryRisingCities(RISING_REGION_AVG_5MONTH_SQL.formatted(COLUMN_NAME_PM10), regionId);
    }

    @Override
    public Optional<CityStatsResponseDTO> queryCityStatsLastHour(UUID cityId) {
        try (var conn = (DuckDBConnection) DriverManager.getConnection(datasourceUrl);
             PreparedStatement stmt = conn.prepareStatement(CITY_STATS_LAST_HOUR_SQL)) {
            stmt.setObject(1, cityId);

            ResultSet resultSet = stmt.executeQuery();
            resultSet.next();

            if (!containsValues(resultSet)) {
                return Optional.empty();
            } else {
                return Optional.of(new CityStatsResponseDTO(
                        getBigDecimal(resultSet, "avg_no2"),
                        getBigDecimal(resultSet, "max_no2"),
                        getBigDecimal(resultSet, "min_no2"),
                        getBigDecimal(resultSet, "avg_co"),
                        getBigDecimal(resultSet, "max_co"),
                        getBigDecimal(resultSet, "min_co"),
                        getBigDecimal(resultSet, "avg_pm10"),
                        getBigDecimal(resultSet, "max_pm10"),
                        getBigDecimal(resultSet, "min_pm10")
                ));
            }
        } catch (SQLException ex) {
            throw new DatastoreException("Problems occurred when accessing database", ex);
        }
    }

    private static boolean containsValues(ResultSet resultSet) throws SQLException {
        return getBigDecimal(resultSet, "avg_no2") != null;
    }

    private ArrayList<String> queryRisingCities(String sql, UUID regionId) {
        var results = new ArrayList<String>();
        try (var conn = (DuckDBConnection) DriverManager.getConnection(datasourceUrl);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, regionId);

            ResultSet resultSet = stmt.executeQuery();
            while (resultSet.next()) {
                results.add(resultSet.getString("name"));
            }
            return results;
        } catch (SQLException ex) {
            throw new DatastoreException("Problems occurred when accessing database", ex);
        }
    }

    private static BigDecimal getBigDecimal(ResultSet rs, String columnName) throws SQLException {
        String value = rs.getString(columnName);
        return rs.wasNull() ? null : new BigDecimal(value);
    }
}
