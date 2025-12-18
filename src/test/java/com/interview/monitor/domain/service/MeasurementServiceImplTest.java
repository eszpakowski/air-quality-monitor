package com.interview.monitor.domain.service;

import com.interview.monitor.adapters.inbound.rest.dto.RisingCityStatsResponseDTO;
import com.interview.monitor.domain.model.Measurement;
import com.interview.monitor.domain.ports.outbound.MeasurementRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static com.interview.monitor.testutils.TestConstants.RADOM_CITY_ID;
import static com.interview.monitor.testutils.TestConstants.WARSZAWA_CITY_ID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MeasurementServiceImplTest {
    @Mock
    MeasurementRepository measurementRepository;

    @InjectMocks
    MeasurementServiceImpl underTest;

    @Test
    void save_shouldCallMeasurementRepository() {
        // given
        var measurement = createValidMeasurement();

        // when
        underTest.save(measurement);

        // then
        verify(measurementRepository).save(measurement);
    }

    @Test
    void calculateRisingCityStats_shouldCallMeasurementRepository() {
        // given
        UUID regionId = UUID.randomUUID();
        List<String> risingCO5MCities = List.of(WARSZAWA_CITY_ID.toString());
        given(measurementRepository.queryRisingCO5MCities(regionId)).willReturn(risingCO5MCities);
        List<String> risingPM105MCities = List.of(RADOM_CITY_ID.toString());
        given(measurementRepository.queryRisingPM105MCities(regionId)).willReturn(risingPM105MCities);

        // when
        RisingCityStatsResponseDTO actual = underTest.calculateRisingCityStats(regionId);

        // then
        var expected = new RisingCityStatsResponseDTO(risingCO5MCities, risingPM105MCities);
        assertEquals(expected, actual);

        verify(measurementRepository).queryRisingCO5MCities(regionId);
        verify(measurementRepository).queryRisingPM105MCities(regionId);
    }

    private static Measurement createValidMeasurement() {
        return new Measurement(null, UUID.randomUUID(), UUID.randomUUID(),
                new BigDecimal("23.1"), new BigDecimal("12.4"), new BigDecimal("0.39"), Instant.now());
    }
}