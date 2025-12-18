# Air Quality Monitor
Spring Boot application for monitoring air quality across European cities. Showcases the benefits of using an embedded 
OLAP database when implementing various analytical workloads.

## Main technologies
- [JDK 21](https://www.oracle.com/pl/java/technologies/downloads/)
- [Spring Boot 4.0](https://spring.io/blog/2025/11/20/spring-boot-4-0-0-available-now)
- [Maven 3](https://maven.apache.org)
- [Flyway](https://github.com/flyway/flyway) - Used for database migration.
- [DuckDB](https://duckdb.org/) - Embedded analytical database (OLAP).

## Building and Running
```shell
# Build the project
mvn clean package

# Run database migrations
mvn flyway:migrate

# Run the application
mvn spring-boot:run

# Run all tests (includes migrating database schema needed for integration tests)
mvn verify
```

## API Endpoints
### POST /api/save-measure
Save an air quality measurement.
Request Body:
```json
{
    "sensorId": "2a83e44f-55c5-480e-a5ee-2bab5c04a597",
    "cityId": "75d6753b-5f93-4db8-a9cd-506b6115b93d",
    "PM10": "23.1",
    "CO": "12.4",
    "NO2": "0.39",
    "timestamp": 1742332375
}
```
### GET /api/stats/1H/city/{cityId}
Get hourly air quality statistics for a specific city.
Response:
```json
{
    "avgNO2LastHour": "23.1",
    "maxNO2LastHour": "22.31",
    "minNO2LastHour": "21.24",
    "avgCOLastHour": "12.43",
    "maxCOLastHour": "12.46",
    "minCOLastHour": "12.39",
    "avgPM10LastHour": "0.29",
    "maxPM10LastHour": "0.3",
    "minPM10LastHour": "0.27"
}
```
### GET /api/stats/5M/{regionId}
Get cities with rising 5-month trends (worsening air quality) in the specified region.
Response:
```json
{
    "risingCO5MCities": ["Ruda Slaska", "Gliwice"],
    "risingPM105MCities": ["Katowice"]
}
```
### GET /api/report/worst-cities-no2-y2y
Get cities where NO2 average for the previous month is higher than the same month a year ago.
Response:
```json
[
    {
        "city": "Warszawa",
        "cityId": "da340572-6ad1-4bba-a1a0-a8971a2b45ab",
        "country": "Poland",
        "avgNo2Current": "0.32",
        "avgNo2YearBefore": "0.29"
    }
]
```

## Scheduled Tasks
1. List of cities is being fetched each day at midnight from a designated endpoint.
- Endpoint is configurable via property `integrations.city-information.base-url`
2. On the first day of each month at midnight, the application generates a CSV report:
- File: `WORST_CITIES_PM10_yyyyMM.csv`
- Contains the top 10 cities with the highest average PM10 from the previous month
- Report generation location is configurable via property `reports.monthly.highest-pm10.location`

## Discussion/comment
- All the heavy lifting is delegated to the OLAP database engine, which seems tailored for this exact class of problems.
- I needed to use classic JDBC and java.sql.PreparedStatements because that's what is suggested by DuckDB docs. It's 
  possible to use some wrapper over JDBC like JdbcTemplate, but then you lose access to low level APIs like [Appender](https://duckdb.org/docs/stable/data/appender).
- Integration tests need to be run via 'mvn verify' since Flyway doesn't seem to work with DuckDb when ran 
  programmatically. I think the reason is it doesn't care about connection limit, while DuckDB is very strict about it.

TODO:
- Swagger documentation.
- Gatling load tests with the expected amount of data.
- More corner cases covered by tests, especially regarding complex SQL queries.
- Think how to improve the suggested endpoint design to get closer to [Richardson Maturity Model](https://martinfowler.com/articles/richardsonMaturityModel.html).
- Try alternative wrappers for JDBC, decide if it makes sense to use them with DuckDB.