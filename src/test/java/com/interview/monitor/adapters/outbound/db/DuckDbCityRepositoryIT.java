package com.interview.monitor.adapters.outbound.db;

import com.interview.monitor.domain.model.City;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ActiveProfiles("test")
class DuckDbCityRepositoryIT {
    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    DuckDbCityRepository underTest;

    @Test
    void shouldCreateExpectedEntities() {
        // given
        var cities = List.of(
                new City(UUID.randomUUID(), "City1", "Country1", "Region1", UUID.randomUUID()),
                new City(UUID.randomUUID(), "City2", "Country2", "Region2", UUID.randomUUID()),
                new City(UUID.randomUUID(), "City3", "Country3", "Region3", UUID.randomUUID())
        );

        Integer countBefore = countCities();

        // when
        underTest.saveAll(cities);

        // then
        Integer countAfter = countCities();
        assertEquals(countBefore + 3, countAfter);
    }

    @Test
    void shouldUpdateExistingEntity() {
        // given
        var id = UUID.randomUUID();
        var regionId = UUID.randomUUID();

        City cityBefore = new City(id, "City", "Country", "Region", regionId);
        underTest.saveAll(List.of(cityBefore));

        // when
        City cityUpdated = new City(id, "City", "Country", "RegionChanged", regionId);
        underTest.saveAll(List.of(cityUpdated));

        // then
        String actual = selectCityRegion(id);
        assertEquals("RegionChanged", actual);
    }

    private Integer countCities() {
        return jdbcTemplate.queryForObject("SELECT count(*) from cities", Integer.class);
    }

    private String selectCityRegion(UUID id) {
        return jdbcTemplate.queryForObject("SELECT region from cities WHERE id = ?", String.class, id);
    }

}