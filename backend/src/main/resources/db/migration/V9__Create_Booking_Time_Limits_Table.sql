CREATE SEQUENCE booking_time_limit_id_seq START 1;
CREATE TABLE booking_time_limits (
                                     id integer default nextval('booking_time_limit_id_seq') primary key,
                                     max_days_in_advance INTEGER NOT NULL DEFAULT 30,
                                     max_hours_per_week INTEGER NOT NULL DEFAULT 20,
                                     is_active BOOLEAN NOT NULL DEFAULT true
);

ALTER TABLE booking_time_limits
    ADD CONSTRAINT check_max_days_in_advance CHECK (max_days_in_advance > 0 AND max_days_in_advance <= 365),
    ADD CONSTRAINT check_max_hours_per_week CHECK (max_hours_per_week > 0 AND max_hours_per_week <= 168);
-- Unique index to ensure only one active policy at a time
CREATE UNIQUE INDEX idx_booking_time_limits_active
    ON booking_time_limits (is_active)
    WHERE is_active = true;



INSERT INTO booking_time_limits (
    max_days_in_advance,
    max_hours_per_week,
    is_active
) VALUES (30, 20, true);
