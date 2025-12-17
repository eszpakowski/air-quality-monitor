package com.interview.monitor.adapters.inbound.rest;

import com.interview.monitor.adapters.inbound.rest.dto.MeasurementRequestDTO;
import com.interview.monitor.domain.model.Measurement;
import com.interview.monitor.domain.ports.inbound.MeasurementService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MeasurementController.class)
class MeasurementControllerTest {
    private static final String SAVE_MEASURE = "/api/save-measure";
    private static final String CONTENT_TYPE = "Content-type";

    private static final String SENSOR_ID = UUID.randomUUID().toString();
    private static final String CITY_ID = UUID.randomUUID().toString();
    private static final BigDecimal PM_10 = new BigDecimal("23.1");
    private static final BigDecimal CO = new BigDecimal("12.4");
    private static final BigDecimal NO_2 = new BigDecimal("0.39");
    private static final long TIMESTAMP = 1742332376L;

    @MockitoBean
    MeasurementService measurementService;

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    void shouldReturnClientError_whenIncorrectContentType() throws Exception {
        // given
        var request = new MeasurementRequestDTO(SENSOR_ID, CITY_ID, PM_10, CO, NO_2, TIMESTAMP);

        //when & then
        mockMvc.perform(post(SAVE_MEASURE)
                        .header(CONTENT_TYPE, "application/xml")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is4xxClientError())
                .andExpect(content().string("{\"status\":400,\"message\":\"Content-Type 'application/xml' is not supported\"}"));
    }

    @Test
    void shouldReturnClientError_whenMissingRequiredFields() throws Exception {
        // given
        var request = new MeasurementRequestDTO(null, CITY_ID, PM_10, CO, NO_2, TIMESTAMP);

        //when & then
        mockMvc.perform(post(SAVE_MEASURE)
                        .header(CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is4xxClientError())
                .andExpect(content().string("{\"status\":400," +
                                            "\"message\":\"Request validation failed. Reason: sensorId is required\"}"));
    }

    @Test
    void shouldReturnClientError_whenInvalidRequestFormat() throws Exception {
        // given
        var request = new MeasurementRequestDTO("null", CITY_ID, PM_10, CO, NO_2, TIMESTAMP);

        //when & then
        mockMvc.perform(post(SAVE_MEASURE)
                        .header(CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is4xxClientError())
                .andExpect(content().string("{\"status\":400," +
                                            "\"message\":\"Incorrect request format: Invalid UUID string: null\"}"));
    }

    @Test
    void shouldProcessValidRequest() throws Exception {
        // given
        var request = new MeasurementRequestDTO(SENSOR_ID, CITY_ID, PM_10, CO, NO_2, TIMESTAMP);

        //when & then
        mockMvc.perform(post(SAVE_MEASURE)
                        .header(CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is2xxSuccessful());

        verify(measurementService).save(Measurement.fromRequest(request));
    }

}