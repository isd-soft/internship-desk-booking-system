CREATE TABLE Booking(
    id INT PRIMARY KEY SERIAL,
    user_id INT NOT NULL,
    desk_id INT NOT NULL,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    status VARCHAR(20) DEFAULT 'CONFIRMED',

    CONSTRAINT fk_booking_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_booking_desk FOREIGN KEY (desk_id) REFERENCES desks(id) ON DELETE CASCADE,
    CONSTRAINT chk_booking_time CHECK ( end_time > start_time ),
    CONSTRAINT chk_booking_status CHECK (status in (CANCELLED, ACTIVE, CONFIRMED ))
)