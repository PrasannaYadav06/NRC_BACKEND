-- Stage 5 - backfill nr.nrc_followup from nr.followup (VISIT rows) and
-- nr.followup_schedule (SCHEDULE rows). Idempotent (guarded by NOT EXISTS
-- on legacy ids). Source tables are read-only here.
--
-- Run against the LOCAL SQLEXPRESS nrc_live copy only, after explicit
-- review/approval - not yet executed as of writing this file. Run AFTER
-- V11__create_nrc_followup.sql.

-- ===== VISIT rows (completed follow-ups) =====
;WITH visit_seq AS (
    SELECT f.*, ROW_NUMBER() OVER (PARTITION BY f.case_id ORDER BY f.followup_date, f.followup_id) AS rn
    FROM nr.followup f
)
INSERT INTO nr.nrc_followup (
    case_id, record_type, followup_seq, followup_date, place_id, status_id, followup_sd, done_by, remarks,
    form_status_at_event, legacy_followup_id, legacy_case_id, created_on, created_by, last_updated_on, last_updated_by
)
SELECT
    na.case_id, 'VISIT', vs.rn, vs.followup_date, vs.place_id, vs.status_id, vs.followup_sd, vs.done_by, vs.remarks,
    CASE WHEN vs.rn = 1 THEN 5.0 ELSE 5.0 + (vs.rn - 1) / 10.0 END,
    vs.followup_id, vs.case_id, vs.created_on, vs.created_by, vs.last_updated_on, vs.last_updated_by
FROM visit_seq vs
INNER JOIN nr.nrc_admission na ON na.legacy_case_id = vs.case_id
WHERE NOT EXISTS (
    SELECT 1 FROM nr.nrc_followup nf WHERE nf.legacy_followup_id = vs.followup_id AND nf.record_type = 'VISIT'
);

-- ===== SCHEDULE rows (due dates) =====
INSERT INTO nr.nrc_followup (
    case_id, record_type, followup_seq, followup_date, is_active,
    legacy_sched_id, legacy_case_id, created_on, created_by, last_updated_on, last_updated_by
)
SELECT
    na.case_id, 'SCHEDULE', fs.followup_id, fs.followup_date, fs.is_active,
    fs.sched_id, fs.case_id, fs.created_on, fs.created_by, fs.last_updated_on, fs.last_updated_by
FROM nr.followup_schedule fs
INNER JOIN nr.nrc_admission na ON na.legacy_case_id = fs.case_id
WHERE NOT EXISTS (
    SELECT 1 FROM nr.nrc_followup nf WHERE nf.legacy_sched_id = fs.sched_id AND nf.record_type = 'SCHEDULE'
);

-- Backfill nr.nrc_admission.last_followup_date / next_followup_date (added
-- in Stage 3, left NULL until Stage 5 shipped) from the migrated data.
;WITH latest_visit AS (
    SELECT case_id, MAX(followup_date) AS last_date
    FROM nr.nrc_followup WHERE record_type = 'VISIT'
    GROUP BY case_id
),
next_due AS (
    SELECT case_id, MIN(followup_date) AS next_date
    FROM nr.nrc_followup
    WHERE record_type = 'SCHEDULE' AND is_active = 1 AND followup_date > GETDATE()
    GROUP BY case_id
)
UPDATE na
SET na.last_followup_date = lv.last_date,
    na.next_followup_date = nd.next_date
FROM nr.nrc_admission na
LEFT JOIN latest_visit lv ON lv.case_id = na.case_id
LEFT JOIN next_due nd ON nd.case_id = na.case_id
WHERE na.last_followup_date IS NULL AND (lv.last_date IS NOT NULL OR nd.next_date IS NOT NULL);

-- Advance form_status on nr.nrc_admission to the latest VISIT's status for
-- cases that have at least one recorded follow-up (per spec: followup ->
-- 5.1, 5.2, 5.3... - only applies to cases already >= 4 i.e. discharged).
;WITH latest_status AS (
    SELECT case_id, MAX(form_status_at_event) AS latest_status
    FROM nr.nrc_followup WHERE record_type = 'VISIT'
    GROUP BY case_id
)
UPDATE na
SET na.form_status = ls.latest_status
FROM nr.nrc_admission na
INNER JOIN latest_status ls ON ls.case_id = na.case_id
WHERE na.form_status < ls.latest_status;
