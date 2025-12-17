package com.interview.monitor.domain.ports.outbound;

import com.interview.monitor.domain.model.City;

import java.util.List;

public interface CityRepository {
    void saveAll(List<City> cities);
}
