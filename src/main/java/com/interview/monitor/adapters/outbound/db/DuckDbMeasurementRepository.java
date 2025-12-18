package com.interview.monitor.adapters.outbound.db;

import com.interview.monitor.adapters.inbound.rest.dto.CityNo2YearToYearResponseDTO;
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

import static com.interview.monitor.adapters.outbound.db.SqlQueries.*;

@Repository
public class DuckDbMeasurementRepository implements MeasurementRepository {
    private static final String ID = "id";
    private static final String COUNTRY = "country";
    private static final String NAME = "name";
    private static final String COLUMN_NAME_CO = "co";
    private static final String COLUMN_NAME_PM10 = "pm10";
    private static final String AVG_NO_2 = "avg_no2";
    private static final String MAX_NO_2 = "max_no2";
    private static final String MIN_NO_2 = "min_no2";
    private static final String AVG_CO = "avg_co";
    private static final String MAX_CO = "max_co";
    private static final String MIN_CO = "min_co";
    private static final String AVG_PM_10 = "avg_pm10";
    private static final String MAX_PM_10 = "max_pm10";
    private static final String MIN_PM_10 = "min_pm10";
    private static final String AVG_NO_2_CURRENT = "avgNo2Current";
    private static final String AVG_NO_2_YEAR_BEFORE = "avgNo2YearBefore";
    private static final String PROBLEMS_OCCURRED_WHEN_ACCESSING_DATABASE = "Problems occurred when accessing database";

    private final String datasourceUrl;

    public DuckDbMeasurementRepository(@Value("${spring.datasource.url}") String datasourceUrl) {
        this.datasourceUrl = datasourceUrl;
    }

    @Override
    public void save(Measurement measurement) {
        try (var conn = getDuckDBConnection();
             PreparedStatement stmt = conn.prepareStatement(INSERT_MEASUREMENT_SQL)) {
            stmt.setObject(1, measurement.sensorId());
            stmt.setObject(2, measurement.cityId());
            stmt.setBigDecimal(3, measurement.pm10());
            stmt.setBigDecimal(4, measurement.co());
            stmt.setBigDecimal(5, measurement.no2());
            stmt.setTimestamp(6, Timestamp.from(measurement.timestamp()));

            stmt.execute();
        } catch (SQLException ex) {
            throw new DatastoreException(PROBLEMS_OCCURRED_WHEN_ACCESSING_DATABASE, ex);
        }
    }

    @Override
    public void saveAll(List<Measurement> measurements) {
        try (var conn = getDuckDBConnection();
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
            throw new DatastoreException(PROBLEMS_OCCURRED_WHEN_ACCESSING_DATABASE, ex);
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
        try (var conn = getDuckDBConnection();
             PreparedStatement stmt = conn.prepareStatement(CITY_STATS_LAST_HOUR_SQL)) {
            stmt.setObject(1, cityId);

            ResultSet resultSet = stmt.executeQuery();
            resultSet.next();

            if (!containsValues(resultSet)) {
                return Optional.empty();
            } else {
                return Optional.of(new CityStatsResponseDTO(
                        getBigDecimal(resultSet, AVG_NO_2),
                        getBigDecimal(resultSet, MAX_NO_2),
                        getBigDecimal(resultSet, MIN_NO_2),
                        getBigDecimal(resultSet, AVG_CO),
                        getBigDecimal(resultSet, MAX_CO),
                        getBigDecimal(resultSet, MIN_CO),
                        getBigDecimal(resultSet, AVG_PM_10),
                        getBigDecimal(resultSet, MAX_PM_10),
                        getBigDecimal(resultSet, MIN_PM_10)
                ));
            }
        } catch (SQLException ex) {
            throw new DatastoreException(PROBLEMS_OCCURRED_WHEN_ACCESSING_DATABASE, ex);
        }
    }

    @Override
    public void generateMonthlyHighestPM10Report(String filename) {
        try (var conn = getDuckDBConnection();
             PreparedStatement stmt = conn.prepareStatement(GENERATE_MONTHLY_HIGHEST_PM10_REPORT_SQL.formatted(filename))) {
            stmt.execute();
        } catch (SQLException ex) {
            throw new DatastoreException(PROBLEMS_OCCURRED_WHEN_ACCESSING_DATABASE, ex);
        }
    }

    @Override
    public List<CityNo2YearToYearResponseDTO> queryWorstNo2CitiesYearToYear() {
        var results = new ArrayList<CityNo2YearToYearResponseDTO>();
        try (var conn = getDuckDBConnection();
             PreparedStatement stmt = conn.prepareStatement(HIGHER_NO2_CITIES_PREVIOUS_MONTH_YEAR_TO_YEAR_SQL)) {

            ResultSet resultSet = stmt.executeQuery();
            while (resultSet.next()) {
                var cityNo2 = new CityNo2YearToYearResponseDTO(
                        resultSet.getString(NAME),
                        resultSet.getObject(ID, UUID.class),
                        resultSet.getString(COUNTRY),
                        resultSet.getBigDecimal(AVG_NO_2_CURRENT),
                        resultSet.getBigDecimal(AVG_NO_2_YEAR_BEFORE)
                );
                results.add(cityNo2);
            }
            return results;
        } catch (SQLException ex) {
            throw new DatastoreException(PROBLEMS_OCCURRED_WHEN_ACCESSING_DATABASE, ex);
        }
    }

    private ArrayList<String> queryRisingCities(String sql, UUID regionId) {
        var results = new ArrayList<String>();
        try (var conn = getDuckDBConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, regionId);

            ResultSet resultSet = stmt.executeQuery();
            while (resultSet.next()) {
                results.add(resultSet.getString(NAME));
            }
            return results;
        } catch (SQLException ex) {
            throw new DatastoreException(PROBLEMS_OCCURRED_WHEN_ACCESSING_DATABASE, ex);
        }
    }

    private static boolean containsValues(ResultSet resultSet) throws SQLException {
        // We check any of the values to decide if the query returned results
        return getBigDecimal(resultSet, AVG_NO_2) != null;
    }

    private static BigDecimal getBigDecimal(ResultSet rs, String columnName) throws SQLException {
        String value = rs.getString(columnName);
        return rs.wasNull() ? null : new BigDecimal(value);
    }

    private DuckDBConnection getDuckDBConnection() throws SQLException {
        return (DuckDBConnection) DriverManager.getConnection(datasourceUrl);
    }
}
