package com.interview.monitor.domain.ports.outbound;

import com.interview.monitor.adapters.inbound.rest.dto.CityStatsResponseDTO;
import com.interview.monitor.domain.model.Measurement;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MeasurementRepository {
    void save(Measurement measurement);

    void saveAll(List<Measurement> measurements);

    List<String> queryRisingCO5MCities(UUID regionId);

    List<String> queryRisingPM105MCities(UUID regionId);

    Optional<CityStatsResponseDTO> queryCityStatsLastHour(UUID cityId);

    void generateMonthlyHighestPM10Report(String filename);
}
