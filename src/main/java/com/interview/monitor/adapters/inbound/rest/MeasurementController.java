package com.interview.monitor.adapters.inbound.rest;


import com.interview.monitor.adapters.inbound.rest.dto.MeasurementRequestDTO;
import com.interview.monitor.domain.exception.ValidationException;
import com.interview.monitor.domain.model.Measurement;
import com.interview.monitor.domain.ports.inbound.MeasurementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class MeasurementController {
    private final MeasurementService measurementService;

    @PostMapping(value = "/save-measure", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> saveMeasurement(@Valid @RequestBody MeasurementRequestDTO request) {
        log.info("New measurement received [sensorId=%s, cityId=%s, ts=%d]"
                .formatted(request.sensorId(), request.cityId(), request.timestamp()));

        Measurement measurement;
        try {
            measurement = Measurement.fromRequest(request);
        } catch (RuntimeException e) {
            throw new ValidationException("Incorrect request format: %s".formatted(e.getMessage()));
        }

        measurementService.save(measurement);
        return ResponseEntity.ok().build();
    }
}
