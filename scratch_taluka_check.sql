SELECT t.taluka_id, t.taluka_name, d.district_name
FROM config.taluka t
JOIN config.district d ON d.district_id = t.district_id
WHERE d.district_name = 'Chitradurga'
ORDER BY t.taluka_name;
