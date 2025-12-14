CREATE TABLE IF NOT EXISTS measurements (
    id BIGINT PRIMARY KEY,
    sensor_id UUID NOT NULL,
    city_id UUID NOT NULL,
    pm10 DECIMAL(10, 2) NOT NULL,
    co DECIMAL(10, 2) NOT NULL,
    no2 DECIMAL(10, 2) NOT NULL,
    timestamp TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS cities (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    country VARCHAR(255) NOT NULL,
    region VARCHAR(255) NOT NULL,
    region_id UUID NOT NULL
);

CREATE SEQUENCE measurements_seq START 1;