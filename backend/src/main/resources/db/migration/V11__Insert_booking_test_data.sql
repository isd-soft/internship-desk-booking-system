INSERT INTO booking (user_id, desk_id, start_time, end_time)
VALUES
(
    1,
    1,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP + INTERVAL '9 hours'
),
(
    2,
    2,
    CURRENT_TIMESTAMP + INTERVAL '4 hours',
    CURRENT_TIMESTAMP + INTERVAL '5 hours'
),
(
    3,
    20,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP + INTERVAL '2 hours'
),
(
    4,
    32,
    CURRENT_TIMESTAMP + INTERVAL '2 hours',
    CURRENT_TIMESTAMP + INTERVAL '5 hours'
),
(
    5,
    60,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP + INTERVAL '6 hours'
),
(
    6,
    15,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP + INTERVAL '8 hours'
),
(
    7,
    31,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP + INTERVAL '9 hours'
),
(
    8,
    52,
    CURRENT_TIMESTAMP + INTERVAL '4',
    CURRENT_TIMESTAMP + INTERVAL '9 hours'
),
(
    9,
    52,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP + INTERVAL '4 hours'
),
(
    10,
    11,
    CURRENT_TIMESTAMP + INTERVAL '2 hours',
    CURRENT_TIMESTAMP + INTERVAL '6 hours'
);