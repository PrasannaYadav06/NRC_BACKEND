-- Stage 6 - backfills nr.nrc_hr from nr.hospital_details, pivoting the old
-- EAV rows (one per hr_type_id) into one flattened row per (h_id, month,
-- year). Includes hr_type_id 21-24 (MSW) - real historical data (220 rows,
-- back to 2020) that the currently-live frontend/ResourceService path
-- never touches but must not be dropped here (see V15's note).
--
-- Run against the LOCAL SQLEXPRESS nrc_live copy only, after explicit
-- review/approval - not yet executed as of writing this file.

INSERT INTO nr.nrc_hr (
    h_id, month, year, number_of_beds,
    officer_sanctioned, officer_working, officer_vacant, officer_training,
    nurse_sanctioned, nurse_working, nurse_vacant, nurse_training,
    dietician_sanctioned, dietician_working, dietician_vacant, dietician_training,
    cook_sanctioned, cook_working, cook_vacant, cook_training,
    tender_sanctioned, tender_working, tender_vacant, tender_training,
    msw_sanctioned, msw_working, msw_vacant, msw_training,
    legacy_details_ids, created_on
)
SELECT
    h_id, month, year,
    MAX(CASE WHEN hr_type_id = 25 THEN value END) AS number_of_beds,

    MAX(CASE WHEN hr_type_id = 1 THEN value END) AS officer_sanctioned,
    MAX(CASE WHEN hr_type_id = 2 THEN value END) AS officer_working,
    MAX(CASE WHEN hr_type_id = 3 THEN value END) AS officer_vacant,
    MAX(CASE WHEN hr_type_id = 4 THEN value END) AS officer_training,

    MAX(CASE WHEN hr_type_id = 5 THEN value END) AS nurse_sanctioned,
    MAX(CASE WHEN hr_type_id = 6 THEN value END) AS nurse_working,
    MAX(CASE WHEN hr_type_id = 7 THEN value END) AS nurse_vacant,
    MAX(CASE WHEN hr_type_id = 8 THEN value END) AS nurse_training,

    MAX(CASE WHEN hr_type_id = 9 THEN value END) AS dietician_sanctioned,
    MAX(CASE WHEN hr_type_id = 10 THEN value END) AS dietician_working,
    MAX(CASE WHEN hr_type_id = 11 THEN value END) AS dietician_vacant,
    MAX(CASE WHEN hr_type_id = 12 THEN value END) AS dietician_training,

    MAX(CASE WHEN hr_type_id = 13 THEN value END) AS cook_sanctioned,
    MAX(CASE WHEN hr_type_id = 14 THEN value END) AS cook_working,
    MAX(CASE WHEN hr_type_id = 15 THEN value END) AS cook_vacant,
    MAX(CASE WHEN hr_type_id = 16 THEN value END) AS cook_training,

    MAX(CASE WHEN hr_type_id = 17 THEN value END) AS tender_sanctioned,
    MAX(CASE WHEN hr_type_id = 18 THEN value END) AS tender_working,
    MAX(CASE WHEN hr_type_id = 19 THEN value END) AS tender_vacant,
    MAX(CASE WHEN hr_type_id = 20 THEN value END) AS tender_training,

    MAX(CASE WHEN hr_type_id = 21 THEN value END) AS msw_sanctioned,
    MAX(CASE WHEN hr_type_id = 22 THEN value END) AS msw_working,
    MAX(CASE WHEN hr_type_id = 23 THEN value END) AS msw_vacant,
    MAX(CASE WHEN hr_type_id = 24 THEN value END) AS msw_training,

    STRING_AGG(CAST(details_id AS VARCHAR(20)), ',') AS legacy_details_ids,
    MAX(created_on) AS created_on
FROM nr.hospital_details
WHERE hr_type_id BETWEEN 1 AND 25
GROUP BY h_id, month, year;
