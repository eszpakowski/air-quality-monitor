package com.interview.monitor.adapters.outbound.db;

import com.interview.monitor.domain.exception.DatastoreException;
import com.interview.monitor.domain.model.City;
import com.interview.monitor.domain.ports.outbound.CityRepository;
import org.duckdb.DuckDBConnection;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

@Repository
public class DuckDbCityRepository implements CityRepository {
    private static final String UPSERT_CITY_SQL = """
            INSERT INTO cities (id, name, country, region, region_id)
            VALUES (?, ?, ?, ?, ?)
            ON CONFLICT (id) DO
            UPDATE SET
                name = EXCLUDED.name,
                country = EXCLUDED.country,
                region = EXCLUDED.region
            """;

    private final String datasourceUrl;

    public DuckDbCityRepository(@Value("${spring.datasource.url}") String datasourceUrl) {
        this.datasourceUrl = datasourceUrl;
    }

    @Override
    public void upsertAll(List<City> cities) {
        try (var conn = (DuckDBConnection) DriverManager.getConnection(datasourceUrl);
             PreparedStatement stmt = conn.prepareStatement(UPSERT_CITY_SQL)) {
            for (City city : cities) {
                stmt.setObject(1, city.id());
                stmt.setObject(2, city.name());
                stmt.setObject(3, city.country());
                stmt.setObject(4, city.region());
                stmt.setObject(5, city.regionId());
                stmt.addBatch();
            }

            stmt.executeBatch();
        } catch (SQLException ex) {
            throw new DatastoreException("Problems occurred when  accessing database", ex);
        }
    }
}
