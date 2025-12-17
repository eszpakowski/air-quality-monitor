package com.interview.monitor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;

@EnableRetry
@SpringBootApplication
public class AirQualityMonitorApplication {

	public static void main(String[] args) {
		SpringApplication.run(AirQualityMonitorApplication.class, args);
	}

}
