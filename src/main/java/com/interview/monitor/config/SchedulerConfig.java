package com.interview.monitor.config;

import com.interview.monitor.domain.ports.inbound.CityService;
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

    private final CityService cityService;

    @Scheduled(cron = ONCE_A_DAY_AT_MIDNIGHT)
    void refreshCityInformation() {
        log.info("Attempting to refresh sensor information for each of the cities");
        cityService.refreshCityInformation();
    }
}
