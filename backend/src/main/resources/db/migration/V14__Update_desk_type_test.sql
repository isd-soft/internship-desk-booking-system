UPDATE desk
SET is_temporarily_available = true,
temporary_available_from = CURRENT_TIMESTAMP,
temporary_available_until = CURRENT_TIMESTAMP + INTERVAL '7 days',
status = 'ACTIVE'
WHERE desk_name IN
(
'ADM-01',
'ADM-02',
'ADM-03',
'MARK-04',
'MARK-05',
'MARK-06'
);