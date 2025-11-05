CREATE SEQUENCE id_desk_seq START 1;
CREATE TABLE desk (
                      id integer default nextval('id_desk_seq') PRIMARY KEY ,
                      desk_name VARCHAR(50) NOT NULL UNIQUE,
                      zone VARCHAR(100) NOT NULL,
                      desk_type VARCHAR(20) NOT NULL DEFAULT 'SHARED',
                      desk_status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
                      is_temporarily_available BOOLEAN NOT NULL DEFAULT false,
                      temporary_available_from TIMESTAMP,
                      temporary_available_until TIMESTAMP,

                      CONSTRAINT chk_desk_type CHECK (desk_type IN ('ASSIGNED', 'SHARED', 'UNAVAILABLE')),
                      CONSTRAINT chk_desk_status CHECK (desk_status IN ('ACTIVE', 'DEACTIVATED'))
);

