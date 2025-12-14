package com.interview.monitor.domain.service;

import com.interview.monitor.domain.model.Measurement;
import com.interview.monitor.domain.ports.outbound.MeasurementRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MeasurementServiceImplTest {
    @Mock
    MeasurementRepository measurementRepository;

    @InjectMocks
    MeasurementServiceImpl underTest;

    @Test
    void shouldCallMeasurementRepository() {
        // given
        var measurement = createValidMeasurement();

        // when
        underTest.save(measurement);

        // then
        verify(measurementRepository).save(measurement);
    }

    private static Measurement createValidMeasurement() {
        return new Measurement(null, UUID.randomUUID(), UUID.randomUUID(),
                new BigDecimal("23.1"), new BigDecimal("12.4"), new BigDecimal("0.39"), Instant.now());
    }
}