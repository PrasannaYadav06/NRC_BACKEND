-- Stage 5 follow-up - V11/V12 missed weight_kg/height_cm, which the
-- followup form actually captures at every visit but which live on
-- nr.child_growth (linked via child_growth.followup_id), not on
-- nr.followup itself - same "measurement lives in the shared growth table,
-- not the parent record" pattern already found for admission/discharge.
-- Additive ALTER (nrc_followup already exists and is populated).
--
-- Source data confirmed: 68,157 nr.child_growth rows have followup_id set,
-- with 68,157 DISTINCT followup_id values - a clean 1:1 match against
-- nr.followup's own 68,157 rows (unlike the schedule table, this really is
-- a reliable foreign key).
--
-- Run against the LOCAL SQLEXPRESS nrc_live copy only, after explicit
-- review/approval - not yet executed as of writing this file.

IF NOT EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('nr.nrc_followup') AND name = 'weight_kg')
BEGIN
    ALTER TABLE nr.nrc_followup ADD
        weight_kg  NUMERIC(6,2) NULL,
        height_cm  NUMERIC(6,2) NULL;
END
