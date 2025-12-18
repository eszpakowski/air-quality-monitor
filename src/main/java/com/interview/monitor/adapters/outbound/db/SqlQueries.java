package com.interview.monitor.adapters.outbound.db;

public final class SqlQueries {
    public static final String INSERT_MEASUREMENT_SQL = """
            INSERT INTO measurements (id, sensor_id, city_id, pm10, co, no2, timestamp)
            VALUES (nextval('measurements_seq'), ?, ?, ?, ?, ?, ?)
            """;

    public static final String UPSERT_CITY_SQL = """
            INSERT INTO cities (id, name, country, region, region_id)
            VALUES (?, ?, ?, ?, ?)
            ON CONFLICT (id) DO
            UPDATE SET
                name = EXCLUDED.name,
                country = EXCLUDED.country,
                region = EXCLUDED.region
            """;

    /**
     * List all cities from the last 5 months when there was a constant upward trend for one of the air quality values,
     * meaning for 5 consecutive months the value of that column was rising for that specific city.
     * Specific column name should be interpolated into the query below (used as avg_val).
     */
    public static final String RISING_REGION_AVG_5MONTH_SQL = """
            WITH monthly_avg AS (
                SELECT
                    c.name,
                    m.city_id,
                    time_bucket(INTERVAL '1 month', m.timestamp) AS month,
                    AVG(m.%s) AS avg_val
                FROM measurements m
                JOIN cities c ON c.id = m.city_id
                WHERE c.region_id = ?
                  AND m.timestamp < DATE_TRUNC('month', CURRENT_DATE)
                  AND m.timestamp >= DATE_TRUNC('month', CURRENT_DATE) - INTERVAL '5 months'
                GROUP BY c.name, m.city_id, time_bucket(INTERVAL '1 month', m.timestamp)
            ),
            with_trend AS (
                SELECT
                    name,
                    city_id,
                    avg_val > LAG(avg_val) OVER (PARTITION BY city_id ORDER BY month) AS is_rising
                FROM monthly_avg
            )
            SELECT name
            FROM with_trend
            GROUP BY name
            HAVING COUNT(*) = 5 AND COUNT(*) FILTER (WHERE is_rising) = 4;
            """;

    /**
     * List air quality statistics from the last hour for the chosen city.
     */
    public static final String CITY_STATS_LAST_HOUR_SQL = """
            SELECT
                MIN(no2) as min_no2,
                AVG(no2) as avg_no2,
                MAX(no2) as max_no2,
                MIN(co) as min_co,
                AVG(co) as avg_co,
                MAX(co) as max_co,
                MIN(pm10) as min_pm10,
                AVG(pm10) as avg_pm10,
                MAX(pm10) as max_pm10,
            FROM measurements m
            WHERE m.city_id = ?
              AND m.timestamp >= NOW() - INTERVAL '1 hour';
            """;

    /**
     * Generate CSV report with 10 cities with the highest average monthly PM10 stats for the previous month.
     * Specific file name and location of the report should be interpolated into the query below.
     */
    public static final String GENERATE_MONTHLY_HIGHEST_PM10_REPORT_SQL = """
            COPY (
                SELECT
                    c.name AS CITY,
                    c.region AS REGION,
                    ROUND(AVG(m.pm10), 2) AS PM10
                FROM measurements m
                JOIN cities c ON m.city_id = c.id
                WHERE m.timestamp >= DATE_TRUNC('month', CURRENT_DATE) - INTERVAL '1 month'
                  AND m.timestamp < DATE_TRUNC('month', CURRENT_DATE)
                GROUP BY ALL
                HAVING COUNT(DISTINCT m.timestamp::DATE) = day(last_day(CURRENT_DATE - INTERVAL '1 month'))
                ORDER BY PM10 DESC
                LIMIT 10
            ) TO '%s' (HEADER);
            """;

    /**
     * Return list of all cities for which the average value of NO2 measurements for the previous month is higher than
     * the same average a year before the previous month (so for instance if query is called in February 2026 we want to
     * compare NO2 ratings for all cities between January 2025 and January 2026).
     */
    public static final String HIGHER_NO2_CITIES_PREVIOUS_MONTH_YEAR_TO_YEAR_SQL = """
            WITH previous_month AS (
                SELECT
                    city_id,
                    AVG(no2) AS avg_no2
                FROM measurements
                WHERE timestamp >= DATE_TRUNC('month', current_date) - INTERVAL '1 month'
                  AND timestamp < DATE_TRUNC('month', current_date)
                GROUP BY city_id
            ),
            year_ago_month AS (
                SELECT
                    city_id,
                    AVG(no2) AS avg_no2
                FROM measurements
                WHERE timestamp >= DATE_TRUNC('month', current_date) - INTERVAL '13 months'
                  AND timestamp < DATE_TRUNC('month', current_date) - INTERVAL '12 months'
                GROUP BY city_id
            )
            SELECT
                c.name,
                c.id,
                c.country,
                pm.avg_no2 AS avgNo2Current,
                yam.avg_no2 AS avgNo2YearBefore
            FROM cities c
            JOIN previous_month pm ON c.id = pm.city_id
            JOIN year_ago_month yam ON c.id = yam.city_id
            WHERE pm.avg_no2 > yam.avg_no2;
            """;
    private SqlQueries() {}
}
