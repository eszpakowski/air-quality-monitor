package com.interview.monitor.domain.service;


import com.interview.monitor.domain.model.Measurement;
import com.interview.monitor.domain.ports.inbound.MeasurementService;
import com.interview.monitor.domain.ports.outbound.MeasurementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MeasurementServiceImpl implements MeasurementService {
    private final MeasurementRepository measurementRepository;

    @Override
    @Transactional
    public void save(Measurement measurement) {
        measurementRepository.save(measurement);
    }
}
