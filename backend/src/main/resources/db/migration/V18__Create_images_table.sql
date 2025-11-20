CREATE SEQUENCE image_id_seq START 1;

CREATE TABLE images(
    id            INTEGER DEFAULT nextval('image_id_seq') PRIMARY KEY,
    file_name     VARCHAR(255),
    content_type  VARCHAR(255),
    image_data    BYTEA,
    is_background BOOLEAN NOT NULL DEFAULT false
);