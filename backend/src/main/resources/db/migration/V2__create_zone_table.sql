CREATE TABLE zone (
    id SERIAL PRIMARY KEY,
    zone_name VARCHAR(100) NOT NULL UNIQUE,
    zone_abbreviation VARCHAR(10) NOT NULL UNIQUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_zone_abbr_length CHECK (LENGTH(zone_abbreviation) <= 10)
);
