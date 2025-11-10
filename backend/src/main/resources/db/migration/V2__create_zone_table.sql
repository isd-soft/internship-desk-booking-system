CREATE TABLE zone (
    id SERIAL PRIMARY KEY,
    zone_name VARCHAR(100) NOT NULL UNIQUE,
    zone_abbreviation VARCHAR(10) NOT NULL UNIQUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_zone_abbr_length CHECK (LENGTH(zone_abbreviation) <= 10)
);

INSERT INTO zone(zone_name, zone_abbreviation)
VALUES
    ('Development', 'DEV'),
    ('Engineering', 'ENG'),
    ('Administration', 'ADM'),
    ('Marketing', 'MARK'),
    ('Power Line Communication', 'PLC'),
    ('Service', 'SER'),
    ('Management', 'MAN'),
    ('Front end', 'FRONT');
