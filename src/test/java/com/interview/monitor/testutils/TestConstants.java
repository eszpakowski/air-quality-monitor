package com.interview.monitor.testutils;

import java.math.BigDecimal;
import java.util.UUID;

public class TestConstants {
    public static final UUID WARSAW_CITY_ID = UUID.fromString("b8095440-dd44-462c-bf2f-cee496b60b0f");
    public static final UUID SENSOR_ID = UUID.randomUUID();
    public static final BigDecimal PM_10 = new BigDecimal("23.1");
    public static final BigDecimal CO = new BigDecimal("12.4");
    public static final BigDecimal NO_2 = new BigDecimal("0.39");
}
