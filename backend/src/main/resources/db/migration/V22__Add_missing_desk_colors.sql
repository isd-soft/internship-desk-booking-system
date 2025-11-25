INSERT INTO desk_color(color_name, color_code, color_meaning)
VALUES
    -- Desk statuses
    ('ORANGE', '#fbbf24', 'SHARED'),
    ('BROWN', '#92400e', 'DELETED'),
    ('TEAL', '#14b8a6', 'ACTIVE'),
    ('SLATE', '#334155', 'DEACTIVATED'),
    -- Booking statuses
    ('ROSE', '#f43f5e', 'CANCELLED'),
    ('LIME', '#84cc16', 'SCHEDULED'),
    ('CYAN', '#06b6d4', 'CONFIRMED')
ON CONFLICT (color_meaning) DO NOTHING;
