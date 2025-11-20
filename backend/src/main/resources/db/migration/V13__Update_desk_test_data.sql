UPDATE desk
SET type = 'ASSIGNED', is_temporarily_available = false
WHERE desk_name IN
(
'MARK-01',
'MARK-02',
'MARK-03',
'MARK-04',
'MARK-05',
'MARK-06',
'MARK-07',
'MARK-08',
'MARK-09'
);