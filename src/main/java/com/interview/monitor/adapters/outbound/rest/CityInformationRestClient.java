package com.interview.monitor.adapters.outbound.rest;

import com.interview.monitor.adapters.outbound.rest.dto.CityInfoDTO;
import com.interview.monitor.adapters.outbound.rest.dto.CityInfoListDTO;
import com.interview.monitor.domain.exception.IntegrationException;
import com.interview.monitor.domain.model.City;
import com.interview.monitor.domain.ports.outbound.CityInformationClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;
import java.util.stream.Collectors;


@Slf4j
@Component
public class CityInformationRestClient implements CityInformationClient {
    private final String baseUrl;
    private final RestClient restClient = RestClient.create();

    public CityInformationRestClient(@Value("${integrations.city-information.base-url}") String baseUrl) {
        this.baseUrl = baseUrl;
    }

    @Override
    @Retryable(value = RestClientException.class, maxAttempts = 5, backoff = @Backoff(delay = 1000))
    public List<City> fetchFullCityInformation() {
        log.info("Fetching full list of cities");
        String uri = baseUrl + "/cities";

        CityInfoListDTO response = restClient.get().uri(uri).retrieve().body(CityInfoListDTO.class);
        if (response == null || CollectionUtils.isEmpty(response.cities())) {
            throw new IntegrationException("Problems occurred when fetching city information!");
        }

        return response.cities().stream()
                .map(CityInformationRestClient::toDomain)
                .collect(Collectors.toList());
    }

    private static City toDomain(CityInfoDTO cityInfo) {
        return new City(cityInfo.cityId(), cityInfo.city(), cityInfo.country(), cityInfo.region(), cityInfo.regionId());
    }
}
