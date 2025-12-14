package com.interview.monitor.domain.ports.inbound;

import com.interview.monitor.domain.model.Measurement;

public interface MeasurementService {
    void save(Measurement measurement);
}
