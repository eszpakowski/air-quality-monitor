package com.interview.monitor.domain.ports.inbound;

import com.interview.monitor.adapters.inbound.rest.dto.CityNo2YearToYearResponseDTO;
import com.interview.monitor.adapters.inbound.rest.dto.CityStatsResponseDTO;
import com.interview.monitor.adapters.inbound.rest.dto.RisingCityStatsResponseDTO;
import com.interview.monitor.domain.model.Measurement;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MeasurementService {
    void save(Measurement measurement);

    RisingCityStatsResponseDTO calculateRisingCityStats(UUID regionId);

    Optional<CityStatsResponseDTO> calculateCityStatsLastHour(UUID cityId);

    void generateMonthlyHighestPM10Report();

    List<CityNo2YearToYearResponseDTO> getWorstNo2CitiesYearToYear();
}
