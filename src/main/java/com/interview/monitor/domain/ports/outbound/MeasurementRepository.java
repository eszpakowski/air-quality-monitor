package com.interview.monitor.domain.ports.outbound;

import com.interview.monitor.domain.model.Measurement;

public interface MeasurementRepository {
    void save(Measurement measurement);
}
