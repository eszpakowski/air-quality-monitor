package com.interview.monitor.adapters.outbound.rest.dto;

import java.time.LocalDateTime;
import java.util.List;

public record CityInfoListDTO(LocalDateTime lastUpdate, List<CityInfoDTO> cities) {
}
