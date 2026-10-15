-- Stage 4 follow-up - backfills the 7 columns V9 added, from nr.child_growth
-- (recorded_at_discharge=1, split by height_cm null-ness) and
-- nr.case_mother_growth (recorded_at_discharge=1). Idempotent guard on
-- discharge_weight_kg being NULL (only fills rows not already backfilled).
-- Same "latest row per case" tie-break as V5/V8 for any case with more than
-- one discharge-time growth row recorded.
--
-- Run against the LOCAL SQLEXPRESS nrc_live copy only, after explicit
-- review/approval - not yet executed as of writing this file. Run AFTER
-- V9__add_discharge_growth_columns.sql.

;WITH latest_discharge_weight_height AS (
    SELECT cg.case_id, cg.weight_kg, cg.height_cm
    FROM nr.child_growth cg
    INNER JOIN (
        SELECT case_id, MAX(rec_id) AS rec_id
        FROM nr.child_growth
        WHERE recorded_at_discharge = 1 AND height_cm IS NOT NULL
        GROUP BY case_id
    ) latest ON latest.case_id = cg.case_id AND latest.rec_id = cg.rec_id
),
latest_min_weight AS (
    SELECT cg.case_id, cg.weight_kg, cg.measurement_date
    FROM nr.child_growth cg
    INNER JOIN (
        SELECT case_id, MAX(rec_id) AS rec_id
        FROM nr.child_growth
        WHERE recorded_at_discharge = 1 AND height_cm IS NULL
        GROUP BY case_id
    ) latest ON latest.case_id = cg.case_id AND latest.rec_id = cg.rec_id
),
latest_mother_discharge_growth AS (
    SELECT mg.case_id, mg.weight_kg, mg.height_cm, mg.muac_cm
    FROM nr.case_mother_growth mg
    INNER JOIN (
        SELECT case_id, MAX(rec_id) AS rec_id
        FROM nr.case_mother_growth
        WHERE recorded_at_discharge = 1
        GROUP BY case_id
    ) latest ON latest.case_id = mg.case_id AND latest.rec_id = mg.rec_id
)
UPDATE nd
SET
    nd.discharge_weight_kg = dwh.weight_kg,
    nd.discharge_height_cm = dwh.height_cm,
    nd.min_weight_kg = mw.weight_kg,
    nd.min_weight_date = mw.measurement_date,
    nd.mother_weight_kg_discharge = mmg.weight_kg,
    nd.mother_height_cm_discharge = mmg.height_cm,
    nd.mother_muac_cm_discharge = mmg.muac_cm
FROM nr.nrc_discharge nd
LEFT JOIN latest_discharge_weight_height dwh ON dwh.case_id = nd.legacy_case_id
LEFT JOIN latest_min_weight mw ON mw.case_id = nd.legacy_case_id
LEFT JOIN latest_mother_discharge_growth mmg ON mmg.case_id = nd.legacy_case_id
WHERE nd.discharge_weight_kg IS NULL;

-- Also seed nrc_admission's target_weight_achieved-adjacent summary: not
-- needed here (target_weight_achieved already migrated in Stage 3 from
-- case_details directly) - no further nrc_admission update required.
