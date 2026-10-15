-- Stage 3 - backfill nr.nrc_admission from the 11 existing split tables.
-- Idempotent via the NOT EXISTS guard on legacy_case_id (safe to re-run;
-- inserts only cases not already migrated). Source tables are read-only
-- here - nothing in this script modifies or deletes any existing table.
--
-- Run against the LOCAL SQLEXPRESS nrc_live copy only, after explicit
-- review/approval - not yet executed as of writing this file. Run AFTER
-- V4__create_nrc_admission.sql.

;WITH admission_growth AS (
    -- One row per case for the admission-time child measurement - MAX(rec_id)
    -- breaks ties if a case was ever double-recorded at admission.
    SELECT cg.case_id, cg.weight_kg, cg.height_cm
    FROM nr.child_growth cg
    INNER JOIN (
        SELECT case_id, MAX(rec_id) AS rec_id
        FROM nr.child_growth
        WHERE recorded_at_admission = 1
        GROUP BY case_id
    ) latest ON latest.case_id = cg.case_id AND latest.rec_id = cg.rec_id
),
mother_admission_growth AS (
    SELECT mg.case_id, mg.weight_kg, mg.height_cm, mg.bmi, mg.muac_cm, mg.hemoglobin,
           mg.neck_circumference_cm, mg.gravida, mg.para, mg.blood_sugar_fasting,
           mg.blood_sugar_random, mg.blood_pressure_sys, mg.blood_pressure_dia, mg.advice
    FROM nr.case_mother_growth mg
    INNER JOIN (
        SELECT case_id, MAX(rec_id) AS rec_id
        FROM nr.case_mother_growth
        WHERE recorded_at_admission = 1
        GROUP BY case_id
    ) latest ON latest.case_id = mg.case_id AND latest.rec_id = mg.rec_id
),
criteria_flat AS (
    SELECT case_id,
        MAX(CASE WHEN criteria_id = 1 THEN 1 ELSE 0 END) AS ac_wfl_wfh_lt3z,
        MAX(CASE WHEN criteria_id = 2 THEN 1 ELSE 0 END) AS ac_wfa_lt3z,
        MAX(CASE WHEN criteria_id = 3 THEN 1 ELSE 0 END) AS ac_muac_lt115,
        MAX(CASE WHEN criteria_id = 4 THEN 1 ELSE 0 END) AS ac_severe_wasting,
        MAX(CASE WHEN criteria_id = 5 THEN 1 ELSE 0 END) AS ac_bl_edema,
        MAX(CASE WHEN criteria_id = 6 THEN 1 ELSE 0 END) AS ac_others,
        MAX(CASE WHEN criteria_id = 6 THEN others_value ELSE NULL END) AS ac_others_value,
        MAX(CASE WHEN criteria_id = 7 THEN 1 ELSE 0 END) AS ac_muac_115_125,
        MAX(CASE WHEN criteria_id = 8 THEN 1 ELSE 0 END) AS ac_wfl_wfh_lt2z,
        MAX(CASE WHEN criteria_id = 9 THEN 1 ELSE 0 END) AS ac_wfl_wfh_lt1z
    FROM nr.admission_criteria
    GROUP BY case_id
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
    FROM nr.admission_comorbid_condition
    GROUP BY case_id
),
social_flat AS (
    SELECT case_id,
        MAX(CASE WHEN social_factors_id = 1 THEN 1 ELSE 0 END) AS sf_poverty,
        MAX(CASE WHEN social_factors_id = 2 THEN 1 ELSE 0 END) AS sf_ignorance,
        MAX(CASE WHEN social_factors_id = 3 THEN 1 ELSE 0 END) AS sf_illiteracy,
        MAX(CASE WHEN social_factors_id = 4 THEN 1 ELSE 0 END) AS sf_migrants,
        MAX(CASE WHEN social_factors_id = 5 THEN 1 ELSE 0 END) AS sf_working_mother,
        MAX(CASE WHEN social_factors_id = 6 THEN 1 ELSE 0 END) AS sf_others,
        MAX(CASE WHEN social_factors_id = 6 THEN others_value ELSE NULL END) AS sf_others_value
    FROM nr.case_mother_social_factors
    GROUP BY case_id
),
-- Daily weight-capture rows (child_growth rows that are neither the
-- admission nor discharge snapshot) - latest one per case feeds
-- last_measured_weight/last_measured_date here; the full list is what
-- V6's nr.nrc_weight_capture backfill copies.
latest_daily_weight AS (
    SELECT cg.case_id, cg.weight_kg, cg.measurement_date
    FROM nr.child_growth cg
    INNER JOIN (
        SELECT case_id, MAX(rec_id) AS rec_id
        FROM nr.child_growth
        WHERE ISNULL(recorded_at_admission, 0) = 0 AND ISNULL(recorded_at_discharge, 0) = 0
        GROUP BY case_id
    ) latest ON latest.case_id = cg.case_id AND latest.rec_id = cg.rec_id
)
INSERT INTO nr.nrc_admission (
    h_id, ip_num, uh_id, sam_num, fiscal_year, admission_type_id, admission_date,
    target_weight, admission_sd, addmited_by_doctor, target_weight_achieved,
    child_rch_id, child_name, child_date_of_birth, child_age_in_months, child_gender,
    child_address_line1, child_address_line2, child_address_line3, child_mobile_number,
    child_alternate_mobile_number, child_category, child_religion_id, child_caste_id,
    child_pincode, child_district_id, child_has_guardian, child_guardian_name,
    mother_rch_id, mother_name, mother_date_of_birth, mother_age,
    mother_education_qualification_id, mother_family_planning,
    unit_id, ward_id,
    referred_from_id, referred_by, referred_by_m_numb,
    risk_category_id, no_of_children, medical_problem, medical_problem_details,
    drugs_prescribed, drugs_prescribed_details, present_dietary_choice, no_of_meals_per_day,
    pregnant_at_present, number_of_pregnancy_months, registered_for_anc,
    breast_feeding_at_present, last_menstruation_date,
    mother_weight_kg_admission, mother_height_cm_admission, mother_bmi_admission,
    mother_muac_cm_admission, mother_hemoglobin_admission, mother_neck_circumference_cm_admission,
    mother_gravida, mother_para, mother_blood_sugar_fasting_admission,
    mother_blood_sugar_random_admission, mother_bp_sys_admission, mother_bp_dia_admission,
    mother_growth_advice_admission,
    admission_weight_kg, admission_height_cm,
    ac_wfl_wfh_lt3z, ac_wfa_lt3z, ac_muac_lt115, ac_severe_wasting, ac_bl_edema, ac_others,
    ac_others_value, ac_muac_115_125, ac_wfl_wfh_lt2z, ac_wfl_wfh_lt1z,
    cc_pneumonia, cc_gastroenteritis, cc_sepsis, cc_chd, cc_cp, cc_renal_disease,
    cc_meningitis, cc_ics, cc_iem, cc_tuberculosis, cc_others, cc_others_value, cc_covid19,
    sf_poverty, sf_ignorance, sf_illiteracy, sf_migrants, sf_working_mother, sf_others, sf_others_value,
    last_measured_weight, last_measured_date,
    form_status,
    legacy_case_id, legacy_child_id, legacy_mother_id,
    created_on, created_by, last_updated_on, last_updated_by
)
SELECT
    cd.h_id, cd.ip_num, cd.uh_id, cd.sam_num, cd.fiscal_year, cd.admission_type_id, cd.admission_date,
    cd.target_weight, cd.admission_sd, cd.addmited_by_doctor, cd.target_weight_achieved,
    chd.rch_id, chd.name, chd.date_of_birth, chd.age_in_months, chd.gender,
    chd.address_line1, chd.address_line2, chd.address_line3, chd.mobile_number,
    chd.alternate_mobile_number, chd.category, chd.religon_id, chd.caste_id,
    chd.pincode, chd.district_id, chd.has_guardian, chd.guardian_name,
    md.rch_id, md.name, md.date_of_birth, md.age,
    md.education_qualification_id, md.family_planning,
    ca.unit_id, ca.ward_id,
    cr.referred_from_id, cr.referred_by, cr.referred_by_m_numb,
    cmd.risk_category_id, cmd.no_of_children, cmd.medical_problem, cmd.medical_problem_details,
    cmd.drugs_prescribed, cmd.drugs_prescribed_details, cmd.present_dietary_choice, cmd.no_of_meals_per_day,
    cmd.pregnant_at_present, cmd.number_of_pregnancy_months, cmd.registered_for_anc,
    cmd.breast_feeding_at_present, cmd.last_menstruation_date,
    mag.weight_kg, mag.height_cm, mag.bmi, mag.muac_cm, mag.hemoglobin, mag.neck_circumference_cm,
    mag.gravida, mag.para, mag.blood_sugar_fasting, mag.blood_sugar_random,
    mag.blood_pressure_sys, mag.blood_pressure_dia, mag.advice,
    ag.weight_kg, ag.height_cm,
    ISNULL(crit.ac_wfl_wfh_lt3z, 0), ISNULL(crit.ac_wfa_lt3z, 0), ISNULL(crit.ac_muac_lt115, 0),
    ISNULL(crit.ac_severe_wasting, 0), ISNULL(crit.ac_bl_edema, 0), ISNULL(crit.ac_others, 0),
    crit.ac_others_value, ISNULL(crit.ac_muac_115_125, 0), ISNULL(crit.ac_wfl_wfh_lt2z, 0), ISNULL(crit.ac_wfl_wfh_lt1z, 0),
    ISNULL(com.cc_pneumonia, 0), ISNULL(com.cc_gastroenteritis, 0), ISNULL(com.cc_sepsis, 0),
    ISNULL(com.cc_chd, 0), ISNULL(com.cc_cp, 0), ISNULL(com.cc_renal_disease, 0),
    ISNULL(com.cc_meningitis, 0), ISNULL(com.cc_ics, 0), ISNULL(com.cc_iem, 0),
    ISNULL(com.cc_tuberculosis, 0), ISNULL(com.cc_others, 0), com.cc_others_value, ISNULL(com.cc_covid19, 0),
    ISNULL(soc.sf_poverty, 0), ISNULL(soc.sf_ignorance, 0), ISNULL(soc.sf_illiteracy, 0),
    ISNULL(soc.sf_migrants, 0), ISNULL(soc.sf_working_mother, 0), ISNULL(soc.sf_others, 0), soc.sf_others_value,
    ldw.weight_kg, ldw.measurement_date,
    CASE WHEN cd.discharge_date IS NOT NULL THEN 4 ELSE 0 END, -- best-effort form_status for pre-existing rows
    cd.case_id, cd.child_id, cd.mother_id,
    cd.created_on, cd.created_by, cd.last_updated_on, cd.last_updated_by
FROM nr.case_details cd
LEFT JOIN nr.child_demography chd ON chd.child_id = cd.child_id
LEFT JOIN nr.mother_demogrphy md ON md.mother_id = cd.mother_id
LEFT JOIN nr.case_allocation ca ON ca.case_id = cd.case_id
LEFT JOIN nr.case_referral cr ON cr.case_id = cd.case_id
LEFT JOIN nr.case_mother_details cmd ON cmd.case_id = cd.case_id
LEFT JOIN mother_admission_growth mag ON mag.case_id = cd.case_id
LEFT JOIN admission_growth ag ON ag.case_id = cd.case_id
LEFT JOIN criteria_flat crit ON crit.case_id = cd.case_id
LEFT JOIN comorbid_flat com ON com.case_id = cd.case_id
LEFT JOIN social_flat soc ON soc.case_id = cd.case_id
LEFT JOIN latest_daily_weight ldw ON ldw.case_id = cd.case_id
WHERE NOT EXISTS (
    SELECT 1 FROM nr.nrc_admission na WHERE na.legacy_case_id = cd.case_id
);
