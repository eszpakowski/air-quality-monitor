package com.interview.monitor.adapters.outbound.rest;

import com.interview.monitor.adapters.outbound.rest.dto.CityInfoListDTO;
import com.interview.monitor.domain.exception.IntegrationException;
import com.interview.monitor.domain.model.City;
import com.interview.monitor.domain.ports.outbound.CityInformationClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.restclient.test.autoconfigure.RestClientTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClientException;
import org.wiremock.spring.EnableWireMock;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.assertEquals;

@EnableWireMock
@RestClientTest(components = {CityInformationClient.class}, properties = {"integrations.city-information.base-url=http://localhost:${wiremock.server.port}"})
class CityInformationRestClientTest {
    private static final String CITIES = "/cities";
    private static final int EXPECTED_RETRY = 5;
    public static final String CONTENT_TYPE = "Content-Type";

    @Autowired
    CityInformationClient underTest;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    void shouldRetry_whenExceptionIsThrown() {
        // given
        stubFor(any(urlEqualTo(CITIES))
                .willReturn(aResponse().withStatus(HttpStatus.BAD_GATEWAY.value())));

        // when
        assertThatExceptionOfType(RestClientException.class)
                .isThrownBy(() -> underTest.fetchFullCityInformation());

        //then
        verify(exactly(EXPECTED_RETRY), getRequestedFor(urlEqualTo(CITIES)));
    }

    @Test
    void shouldThrow_whenEmptyCityLitIsReturned() {
        // given
        stubFor(any(urlEqualTo(CITIES))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody(objectMapper.writeValueAsString(new CityInfoListDTO(LocalDateTime.now(), List.of())))));

        // when
        assertThatExceptionOfType(IntegrationException.class)
                .isThrownBy(() -> underTest.fetchFullCityInformation());
    }

    @Test
    void shouldThrow_whenEmptyResponseIsReturned() {
        // given
        stubFor(any(urlEqualTo(CITIES))
                .willReturn(aResponse().withStatus(200)));

        // when
        assertThatExceptionOfType(IntegrationException.class)
                .isThrownBy(() -> underTest.fetchFullCityInformation());
    }

    @Test
    void shouldFetchFullCityInformation() {
        // when
        List<City> actual = underTest.fetchFullCityInformation();

        //then
        assertEquals(4, actual.size());
    }
}