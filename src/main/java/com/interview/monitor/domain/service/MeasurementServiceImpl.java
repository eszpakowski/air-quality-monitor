package com.interview.monitor.domain.service;


import com.interview.monitor.adapters.inbound.rest.dto.CityStatsResponseDTO;
import com.interview.monitor.adapters.inbound.rest.dto.RisingCityStatsResponseDTO;
import com.interview.monitor.domain.model.Measurement;
import com.interview.monitor.domain.ports.inbound.MeasurementService;
import com.interview.monitor.domain.ports.outbound.MeasurementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MeasurementServiceImpl implements MeasurementService {
    private final MeasurementRepository measurementRepository;

    @Override
    @Transactional
    public void save(Measurement measurement) {
        measurementRepository.save(measurement);
    }

    @Override
    public RisingCityStatsResponseDTO calculateRisingCityStats(UUID regionId) {
        List<String> risingCO5MCities = measurementRepository.queryRisingCO5MCities(regionId);
        List<String> risingPM105MCities = measurementRepository.queryRisingPM105MCities(regionId);
        return new RisingCityStatsResponseDTO(risingCO5MCities, risingPM105MCities);
    }

    @Override
    public Optional<CityStatsResponseDTO> calculateCityStatsLastHour(UUID cityId) {
        return measurementRepository.queryCityStatsLastHour(cityId);
    }
}
