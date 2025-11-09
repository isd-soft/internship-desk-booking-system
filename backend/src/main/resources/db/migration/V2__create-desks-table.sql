CREATE SEQUENCE id_desk_seq START 1;
CREATE TABLE desk (
                      id integer default nextval('id_desk_seq') PRIMARY KEY ,
                      desk_name VARCHAR(50) NOT NULL UNIQUE,
                      zone VARCHAR(100) NOT NULL,
                      type VARCHAR(20) NOT NULL DEFAULT 'SHARED',
                      status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
                      is_temporarily_available BOOLEAN NOT NULL DEFAULT false,
                      temporary_available_from TIMESTAMP,
                      temporary_available_until TIMESTAMP,
                      current_x DOUBLE PRECISION,
                      current_y DOUBLE PRECISION,
                      base_x DOUBLE PRECISION,
                      base_y DOUBLE PRECISION,

                      CONSTRAINT chk_desk_type CHECK (type IN ('ASSIGNED', 'SHARED', 'UNAVAILABLE')),
                      CONSTRAINT chk_desk_status CHECK (status IN ('ACTIVE', 'DEACTIVATED'))
);

