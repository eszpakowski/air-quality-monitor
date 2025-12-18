package com.interview.monitor.domain.service;


import com.interview.monitor.adapters.inbound.rest.dto.CityNo2YearToYearResponseDTO;
import com.interview.monitor.adapters.inbound.rest.dto.CityStatsResponseDTO;
import com.interview.monitor.adapters.inbound.rest.dto.RisingCityStatsResponseDTO;
import com.interview.monitor.domain.model.Measurement;
import com.interview.monitor.domain.ports.inbound.MeasurementService;
import com.interview.monitor.domain.ports.outbound.MeasurementRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class MeasurementServiceImpl implements MeasurementService {
    private static final String FILE_PREFIX = "WORST_CITIES_PM10_";
    private static final DateTimeFormatter FILE_NAME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMM");

    private final String reportLocation;
    private final MeasurementRepository measurementRepository;

    public MeasurementServiceImpl(@Value("${reports.monthly.highest-pm10.location}") String reportLocation,
                                  MeasurementRepository measurementRepository) {
        this.reportLocation = reportLocation;
        this.measurementRepository = measurementRepository;
    }

    @Override
    @Transactional
    public void save(Measurement measurement) {
        measurementRepository.save(measurement);
    }

    @Override
    @Transactional(readOnly = true)
    public RisingCityStatsResponseDTO calculateRisingCityStats(UUID regionId) {
        List<String> risingCO5MCities = measurementRepository.queryRisingCO5MCities(regionId);
        List<String> risingPM105MCities = measurementRepository.queryRisingPM105MCities(regionId);
        return new RisingCityStatsResponseDTO(risingCO5MCities, risingPM105MCities);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CityStatsResponseDTO> calculateCityStatsLastHour(UUID cityId) {
        return measurementRepository.queryCityStatsLastHour(cityId);
    }

    @Override
    @Transactional(readOnly = true)
    public void generateMonthlyHighestPM10Report() {
        String fileName = FILE_PREFIX + LocalDate.now().minusMonths(1).format(FILE_NAME_FORMATTER) + ".csv";
        measurementRepository.generateMonthlyHighestPM10Report(reportLocation + fileName);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CityNo2YearToYearResponseDTO> getWorstNo2CitiesYearToYear() {
        return measurementRepository.queryWorstNo2CitiesYearToYear();
    }
}
