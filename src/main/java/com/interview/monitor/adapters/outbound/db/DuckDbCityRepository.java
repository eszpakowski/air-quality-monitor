package com.interview.monitor.adapters.outbound.db;

import com.interview.monitor.domain.model.City;
import com.interview.monitor.domain.ports.outbound.CityRepository;
import org.duckdb.DuckDBConnection;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;

@Repository
public class DuckDbCityRepository implements CityRepository {
    private static final String TABLE_NAME = "cities";

    private final String datasourceUrl;

    public DuckDbCityRepository(@Value("${spring.datasource.url}") String datasourceUrl) {
        this.datasourceUrl = datasourceUrl;
    }

    @Override
    public void saveAll(List<City> cities) {
        try (var conn = (DuckDBConnection) DriverManager.getConnection(datasourceUrl);
             var appender = conn.createAppender(DuckDBConnection.DEFAULT_SCHEMA, TABLE_NAME)) {
            for (City city : cities) {
                appender.beginRow();
                appender.append(city.id());
                appender.append(city.name());
                appender.append(city.country());
                appender.append(city.region());
                appender.append(city.regionId());
                appender.endRow();
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Problems occurred when acquiring connection", ex);
        }
    }
}
