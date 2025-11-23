CREATE SEQUENCE user_id_seq START 1;

CREATE TABLE users
(
    id            integer DEFAULT nextval('user_id_seq') PRIMARY KEY,
    email         varchar(50) NOT NULL UNIQUE,
    role          varchar(50) NOT NULL,
    password_hash varchar(150),
    auth_provider varchar(20) NOT NULL
                          DEFAULT 'LOCAL'
);
