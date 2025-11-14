UPDATE desk
SET status = 'DEACTIVATED'
WHERE desk_name IN
(
'PLC-01',
'PLC-02',
'PLC-03',
'PLC-04',
'PLC-05'
);