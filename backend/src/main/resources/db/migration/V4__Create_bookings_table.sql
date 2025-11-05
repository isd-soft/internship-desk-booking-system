CREATE SEQUENCE booking_id_seq START 1;

CREATE TABLE Booking(
    id integer default nextval('booking_id_seq') primary key,
    user_id INT NOT NULL,
    desk_id INT NOT NULL,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    status VARCHAR(20) DEFAULT 'CONFIRMED'
)