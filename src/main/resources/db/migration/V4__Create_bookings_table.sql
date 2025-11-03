CREATE TABLE bookings(
    id INT PRIMARY KEY SERIAL,
    user_id INT NOT NULL,
    desk_id INT NOT NULL,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    status VARCHAR(20) DEFAULT 'CONFIRMED'
)