package com.interview.monitor.config;

import com.interview.monitor.domain.ports.inbound.CityService;
import com.interview.monitor.domain.ports.inbound.MeasurementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Slf4j
@Configuration
@EnableScheduling
@RequiredArgsConstructor
public class SchedulerConfig {
    private static final String ONCE_A_DAY_AT_MIDNIGHT = "0 0 * * * *";
    private static final String ONCE_A_MONTH_AT_MIDNIGHT_OF_THE_FIRST_DAY_OF_THE_MONTH = "0 0 1 * * *";

    private final CityService cityService;
    private final MeasurementService measurementService;

    @Scheduled(cron = ONCE_A_DAY_AT_MIDNIGHT)
    void refreshCityInformation() {
        log.info("Attempting to refresh sensor information for each of the cities");
        cityService.refreshCityInformation();
    }

    @Scheduled(cron = ONCE_A_MONTH_AT_MIDNIGHT_OF_THE_FIRST_DAY_OF_THE_MONTH)
    void generateMonthlyHighestPM10Report() {
        log.info("Attempting to generate monthly PM10 report");
        measurementService.generateMonthlyHighestPM10Report();
    }
}
