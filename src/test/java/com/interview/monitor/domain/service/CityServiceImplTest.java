package com.interview.monitor.domain.service;

import com.interview.monitor.domain.model.City;
import com.interview.monitor.domain.ports.outbound.CityInformationClient;
import com.interview.monitor.domain.ports.outbound.CityRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CityServiceImplTest {
    @Mock
    CityInformationClient cityInformationClient;
    @Mock
    CityRepository cityRepository;

    @InjectMocks
    CityServiceImpl underTest;

    @Test
    void shouldRefreshCityInformation() {
        // given
        var cities = List.of(
                new City(UUID.randomUUID(), "City1", "Country1", "Region1", UUID.randomUUID()),
                new City(UUID.randomUUID(), "City2", "Country2", "Region2", UUID.randomUUID()),
                new City(UUID.randomUUID(), "City3", "Country3", "Region3", UUID.randomUUID())
        );
        given(cityInformationClient.fetchFullCityInformation()).willReturn(cities);

        // when
        underTest.refreshCityInformation();

        // then
        verify(cityInformationClient).fetchFullCityInformation();
        verify(cityRepository).upsertAll(cities);
    }
}