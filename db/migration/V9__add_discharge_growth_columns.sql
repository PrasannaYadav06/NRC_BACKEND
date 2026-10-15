-- Stage 4 follow-up - V7/V8 missed 3 real fields the discharge form actually
-- sends (discharge-time child weight/height, "minimum weight during stay",
-- and mother's discharge-time growth) because they live on nr.child_growth/
-- nr.case_mother_growth (recorded_at_discharge=1), not on
-- discharge_child_details/discharge_mother_details themselves. Additive
-- ALTER (nrc_discharge already exists and is populated from V7/V8) - new
-- nullable columns only, nothing removed or changed.
--
-- Source data confirmed: nr.child_growth has 125,691 recorded_at_discharge=1
-- rows, split cleanly into two purposes by whether height_cm is populated -
-- 63,624 "minimum weight during stay" markers (height_cm IS NULL, per
-- DischargeService#dischargeCase's minWeightEntity) and 62,067 real
-- discharge weight+height rows (height_cm IS NOT NULL, dischargeWeightEntity)
-- - and nr.case_mother_growth has 10,389 recorded_at_discharge=1 rows for
-- the mother's own discharge-time measurements.
--
-- Run against the LOCAL SQLEXPRESS nrc_live copy only, after explicit
-- review/approval - not yet executed as of writing this file.

IF NOT EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('nr.nrc_discharge') AND name = 'discharge_weight_kg')
BEGIN
    ALTER TABLE nr.nrc_discharge ADD
        discharge_weight_kg           NUMERIC(6,2) NULL,
        discharge_height_cm           NUMERIC(6,2) NULL,
        min_weight_kg                 NUMERIC(6,2) NULL,
        min_weight_date                DATE NULL,
        mother_weight_kg_discharge     NUMERIC(6,2) NULL,
        mother_height_cm_discharge     NUMERIC(6,2) NULL,
        mother_muac_cm_discharge       NUMERIC(6,2) NULL;
END
