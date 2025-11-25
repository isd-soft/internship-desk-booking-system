ALTER TABLE users
    ADD COLUMN is_guest boolean NOT NULL DEFAULT false;
