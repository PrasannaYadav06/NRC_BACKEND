-- Stage 4 - backfill nr.nrc_discharge from the 3 existing split tables +
-- nr.case_details' discharge_date/discharge_sd/discharged_by_doctor
-- columns. Idempotent (safe to re-run - only inserts cases not already
-- migrated). Source tables are read-only here.
--
-- discharge_child_details/discharge_mother_details both contain a small
-- number of case_ids with 2 rows each (15 and 16 respectively, confirmed
-- all within seconds-to-minutes of each other - duplicate-submission
-- artifacts from the old frontend, not genuine distinct re-discharge
-- events) - only the LATEST row per case_id is used here, same
-- MAX(id)-per-case tie-break pattern V5 used for admission/child growth.
-- Nothing is deleted from the source tables; the earlier duplicate rows
-- simply aren't the one selected as "the" discharge record.
--
-- Run against the LOCAL SQLEXPRESS nrc_live copy only, after explicit
-- review/approval - not yet executed as of writing this file. Run AFTER
-- V7__create_nrc_discharge.sql.

;WITH latest_discharge_child AS (
    SELECT dc.*
    FROM nr.discharge_child_details dc
    INNER JOIN (
        SELECT case_id, MAX(discharge_id) AS discharge_id
        FROM nr.discharge_child_details
        GROUP BY case_id
    ) latest ON latest.case_id = dc.case_id AND latest.discharge_id = dc.discharge_id
),
latest_discharge_mother AS (
    SELECT dm.*
    FROM nr.discharge_mother_details dm
    INNER JOIN (
        SELECT case_id, MAX(discharge_id) AS discharge_id
        FROM nr.discharge_mother_details
        GROUP BY case_id
    ) latest ON latest.case_id = dm.case_id AND latest.discharge_id = dm.discharge_id
),
comorbid_flat AS (
    SELECT case_id,
        MAX(CASE WHEN comorbid_id = 1 THEN 1 ELSE 0 END) AS cc_pneumonia,
        MAX(CASE WHEN comorbid_id = 2 THEN 1 ELSE 0 END) AS cc_gastroenteritis,
        MAX(CASE WHEN comorbid_id = 3 THEN 1 ELSE 0 END) AS cc_sepsis,
        MAX(CASE WHEN comorbid_id = 4 THEN 1 ELSE 0 END) AS cc_chd,
        MAX(CASE WHEN comorbid_id = 5 THEN 1 ELSE 0 END) AS cc_cp,
        MAX(CASE WHEN comorbid_id = 6 THEN 1 ELSE 0 END) AS cc_renal_disease,
        MAX(CASE WHEN comorbid_id = 7 THEN 1 ELSE 0 END) AS cc_meningitis,
        MAX(CASE WHEN comorbid_id = 8 THEN 1 ELSE 0 END) AS cc_ics,
        MAX(CASE WHEN comorbid_id = 9 THEN 1 ELSE 0 END) AS cc_iem,
        MAX(CASE WHEN comorbid_id = 10 THEN 1 ELSE 0 END) AS cc_tuberculosis,
        MAX(CASE WHEN comorbid_id = 11 THEN 1 ELSE 0 END) AS cc_others,
        MAX(CASE WHEN comorbid_id = 11 THEN others_value ELSE NULL END) AS cc_others_value,
        MAX(CASE WHEN comorbid_id = 12 THEN 1 ELSE 0 END) AS cc_covid19
    FROM nr.discharge_comorbid_condition
    GROUP BY case_id
)
INSERT INTO nr.nrc_discharge (
    case_id, discharge_date, discharge_sd, discharged_by_doctor,
    avg_weight_gain_kg, clinical_impression, outcome_id, treatment_given, medical_advice, dietary_advice,
    medical_risk_id,
    cc_pneumonia, cc_gastroenteritis, cc_sepsis, cc_chd, cc_cp, cc_renal_disease,
    cc_meningitis, cc_ics, cc_iem, cc_tuberculosis, cc_others, cc_others_value, cc_covid19,
    form_status_at_event,
    legacy_case_id, legacy_discharge_child_id, legacy_discharge_mother_id,
    created_on, created_by, last_updated_on, last_updated_by
)
SELECT
    na.case_id, cd.discharge_date, cd.discharge_sd, cd.discharged_by_doctor,
    ldc.avg_weight_gain_kg, ldc.clinical_impression, ldc.outcome_id, ldc.treatment_given, ldc.medical_advice, ldc.dietary_advice,
    ldm.medical_risk_id,
    ISNULL(cf.cc_pneumonia, 0), ISNULL(cf.cc_gastroenteritis, 0), ISNULL(cf.cc_sepsis, 0),
    ISNULL(cf.cc_chd, 0), ISNULL(cf.cc_cp, 0), ISNULL(cf.cc_renal_disease, 0),
    ISNULL(cf.cc_meningitis, 0), ISNULL(cf.cc_ics, 0), ISNULL(cf.cc_iem, 0),
    ISNULL(cf.cc_tuberculosis, 0), ISNULL(cf.cc_others, 0), cf.cc_others_value, ISNULL(cf.cc_covid19, 0),
    4, -- first/only migrated discharge event per case = form_status 4
    ldc.case_id, ldc.discharge_id, ldm.discharge_id,
    ldc.created_on, ldc.created_by, ldc.last_updated_on, ldc.last_updated_by
FROM latest_discharge_child ldc
INNER JOIN nr.nrc_admission na ON na.legacy_case_id = ldc.case_id
LEFT JOIN nr.case_details cd ON cd.case_id = ldc.case_id
LEFT JOIN latest_discharge_mother ldm ON ldm.case_id = ldc.case_id
LEFT JOIN comorbid_flat cf ON cf.case_id = ldc.case_id
WHERE NOT EXISTS (
    SELECT 1 FROM nr.nrc_discharge nd WHERE nd.legacy_case_id = ldc.case_id
);

-- Backfill nr.nrc_admission.last_discharge_date (added in Stage 3,
-- deliberately left NULL until Stage 4 shipped) from the discharge event
-- just migrated above.
UPDATE na
SET na.last_discharge_date = nd.discharge_date
FROM nr.nrc_admission na
INNER JOIN nr.nrc_discharge nd ON nd.case_id = na.case_id
WHERE na.last_discharge_date IS NULL AND nd.discharge_date IS NOT NULL;
