CREATE SEQUENCE color_id_seq START 1;

CREATE TABLE desk_color(
                           id BIGINT PRIMARY KEY DEFAULT nextval('color_id_seq'),
                           color_name VARCHAR(100) NOT NULL UNIQUE,
                           color_code VARCHAR(10) NOT NULL UNIQUE,
                           color_meaning VARCHAR(100) NOT NULL UNIQUE
);
