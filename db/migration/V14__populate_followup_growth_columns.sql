-- Stage 5 follow-up - backfills weight_kg/height_cm (added by V13) from
-- nr.child_growth, joined via the reliable child_growth.followup_id ->
-- nr.followup.followup_id link (confirmed 1:1, no tie-break needed).
--
-- Run against the LOCAL SQLEXPRESS nrc_live copy only, after explicit
-- review/approval - not yet executed as of writing this file. Run AFTER
-- V13__add_followup_growth_columns.sql.

UPDATE nf
SET nf.weight_kg = cg.weight_kg,
    nf.height_cm = cg.height_cm
FROM nr.nrc_followup nf
INNER JOIN nr.child_growth cg ON cg.followup_id = nf.legacy_followup_id
WHERE nf.record_type = 'VISIT' AND nf.weight_kg IS NULL;
