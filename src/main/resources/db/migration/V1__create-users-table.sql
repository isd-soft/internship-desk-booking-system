CREATE SEQUENCE user_id_seq START 1;

CREATE TABLE users
(
    id            integer default nextval('user_id_seq') primary key,
    first_name    varchar(50) not null,
    last_name     varchar(50) not null,
    email         varchar(50) not null unique,
    role          varchar(50) not null,
    password_hash varchar(150)
);
