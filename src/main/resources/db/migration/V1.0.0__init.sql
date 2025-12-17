CREATE TABLE IF NOT EXISTS cities (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    country VARCHAR(255) NOT NULL,
    region VARCHAR(255) NOT NULL,
    region_id UUID NOT NULL
);
INSERT INTO cities VALUES
('a5e32ed6-e4f9-4a9e-abc5-e380d58f2f90', 'Cieszyn', 'Poland', 'Śląskie', 'bdf77472-dda7-4578-b097-14fbeea6b70e'),
('41959f8d-69f0-4746-b586-a8a0ab8c3ed8', 'Kraków', 'Poland', 'Małopolskie', '599fa14c-8deb-4832-9e48-3088687448d0'),
('6c156b99-71ab-4639-b814-b6e43f6e2dc1', 'Ruda Śląska', 'Poland', 'Śląskie', '14daafa4-2272-4f54-9000-f3125ddb8e2e'),
('b8095440-dd44-462c-bf2f-cee496b60b0f', 'Warszawa', 'Poland', 'Mazowieckie', '77f875e1-d282-424f-bb12-21ddcba412cd');

CREATE TABLE IF NOT EXISTS measurements (
    id BIGINT PRIMARY KEY,
    sensor_id UUID NOT NULL,
    city_id UUID NOT NULL REFERENCES cities(id),
    pm10 DECIMAL(10, 2) NOT NULL,
    co DECIMAL(10, 2) NOT NULL,
    no2 DECIMAL(10, 2) NOT NULL,
    timestamp TIMESTAMP NOT NULL
);
CREATE SEQUENCE measurements_seq START 1;
