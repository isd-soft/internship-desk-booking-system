CREATE SEQUENCE id_desk_seq START 1;
CREATE TABLE desk (
                      id integer default nextval('id_desk_seq') PRIMARY KEY ,
                      desk_name VARCHAR(50) NOT NULL UNIQUE,
                      zone_id INTEGER NOT NULL,
                      zone VARCHAR(100) NOT NULL,
                      type VARCHAR(20) NOT NULL DEFAULT 'SHARED',
                      status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
                      is_temporarily_available BOOLEAN NOT NULL DEFAULT false,
                      temporary_available_from TIMESTAMP,
                      temporary_available_until TIMESTAMP,

                      CONSTRAINT fk_desk_zone FOREIGN KEY (zone_id) REFERENCES zone(id) ON DELETE RESTRICT,
                      CONSTRAINT chk_desk_type CHECK (type IN ('ASSIGNED', 'SHARED', 'UNAVAILABLE')),
                      CONSTRAINT chk_desk_status CHECK (status IN ('ACTIVE', 'DEACTIVATED'))
);

