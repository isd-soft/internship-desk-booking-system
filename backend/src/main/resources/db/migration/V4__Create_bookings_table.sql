CREATE SEQUENCE booking_id_seq START 1;

CREATE TABLE booking (
    id INTEGER DEFAULT nextval('booking_id_seq') PRIMARY KEY,
    user_id INT NOT NULL,
    desk_id INT NOT NULL,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    status VARCHAR(20) DEFAULT 'CONFIRMED',

    CONSTRAINT fk_booking_user
        FOREIGN KEY (user_id)
        REFERENCES users (id)
        ON DELETE CASCADE
);
