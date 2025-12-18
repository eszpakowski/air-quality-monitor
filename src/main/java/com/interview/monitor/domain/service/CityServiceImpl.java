package com.interview.monitor.domain.service;

import com.interview.monitor.domain.model.City;
import com.interview.monitor.domain.ports.inbound.CityService;
import com.interview.monitor.domain.ports.outbound.CityInformationClient;
import com.interview.monitor.domain.ports.outbound.CityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CityServiceImpl implements CityService {
    private final CityInformationClient cityInformationClient;
    private final CityRepository cityRepository;

    @Override
    @Transactional
    public void refreshCityInformation() {
        List<City> cities = cityInformationClient.fetchFullCityInformation();
        cityRepository.upsertAll(cities);
    }
}
