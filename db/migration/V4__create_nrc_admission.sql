-- Stage 3 - In-Patient Care (admission) module consolidation.
-- Additive only: creates ONE new table consolidating the 11 tables listed
-- below (all left untouched, not dropped, as the rollback net per the
-- locked-in migration strategy). One form = one table, matching KABHI's
-- convention (flattened multi-select checkboxes -> individual boolean
-- columns, no child rows for admission_criteria/admission_comorbid_condition/
-- case_mother_social_factors).
--
-- Source tables consolidated here (row counts as of 2026-09-14, local copy):
--   case_details (63,924), child_demography (64,179), mother_demogrphy (64,179),
--   case_allocation (63,924), case_referral (63,924), case_mother_details (27,453),
--   case_mother_growth admission-slice (subset of 40,140), child_growth
--   admission-slice (subset of 674,148), admission_criteria (90,102),
--   admission_comorbid_condition (27,621), case_mother_social_factors (49,518).
--
-- Discharge-time data (discharge_date/discharge_sd/discharged_by_doctor
-- currently sitting on case_details, plus the 3 discharge_* tables) is
-- deliberately NOT copied here - it belongs to nr.nrc_discharge (Stage 4).
-- form_status reaching 4.x is what marks "discharged" on this table; the
-- discharge specifics live in the Stage 4 table, FK'd back to case_id.
--
-- Run against the LOCAL SQLEXPRESS nrc_live copy only, after explicit
-- review/approval - not yet executed as of writing this file.

IF NOT EXISTS (SELECT 1 FROM sys.tables t JOIN sys.schemas s ON t.schema_id = s.schema_id WHERE s.name = 'nr' AND t.name = 'nrc_admission')
BEGIN
    CREATE TABLE nr.nrc_admission (
        case_id                     INT IDENTITY(1,1) PRIMARY KEY,

        -- ===== case_details =====
        h_id                        INT NULL,
        ip_num                      BIGINT NULL,
        uh_id                       BIGINT NULL,
        sam_num                     INT NULL,
        fiscal_year                 VARCHAR(10) NULL,
        admission_type_id           INT NULL,
        admission_date              DATETIME NULL,
        target_weight               NUMERIC(6,2) NULL,
        admission_sd                NUMERIC(6,2) NULL,
        addmited_by_doctor          INT NULL,
        target_weight_achieved      BIT NULL,

        -- ===== child_demography =====
        child_rch_id                BIGINT NULL,
        child_name                  VARCHAR(100) NULL,
        child_date_of_birth         DATE NULL,
        child_age_in_months         INT NULL,
        child_gender                CHAR(1) NULL,
        child_address_line1         VARCHAR(200) NULL,
        child_address_line2         VARCHAR(200) NULL,
        child_address_line3         VARCHAR(200) NULL,
        child_mobile_number         CHAR(10) NULL,
        child_alternate_mobile_number VARCHAR(20) NULL,
        child_category              VARCHAR(6) NULL,
        child_religion_id           INT NULL,
        child_caste_id              INT NULL,
        child_pincode               INT NULL,
        child_district_id           INT NULL,
        child_has_guardian          BIT NULL,
        child_guardian_name         VARCHAR(100) NULL,

        -- ===== mother_demogrphy =====
        mother_rch_id                BIGINT NULL,
        mother_name                  VARCHAR(100) NULL,
        mother_date_of_birth         DATE NULL,
        mother_age                   INT NULL,
        mother_education_qualification_id INT NULL,
        mother_family_planning       BIT NULL,

        -- ===== case_allocation (1:1 per case) =====
        unit_id                     INT NULL,
        ward_id                     INT NULL,

        -- ===== case_referral (1:1 per case) =====
        referred_from_id            INT NULL,
        referred_by                 VARCHAR(100) NULL,
        referred_by_m_numb          VARCHAR(20) NULL,

        -- ===== case_mother_details (1:1 per case) =====
        risk_category_id            INT NULL,
        no_of_children               INT NULL,
        medical_problem              BIT NULL,
        medical_problem_details      VARCHAR(500) NULL,
        drugs_prescribed             BIT NULL,
        drugs_prescribed_details     VARCHAR(500) NULL,
        present_dietary_choice       VARCHAR(100) NULL,
        no_of_meals_per_day          INT NULL,
        pregnant_at_present          BIT NULL,
        number_of_pregnancy_months   NUMERIC(4,1) NULL,
        registered_for_anc           BIT NULL,
        breast_feeding_at_present    BIT NULL,
        last_menstruation_date       DATE NULL,

        -- ===== case_mother_growth, admission-time slice (recorded_at_admission=1) =====
        mother_weight_kg_admission          NUMERIC(6,2) NULL,
        mother_height_cm_admission          NUMERIC(6,2) NULL,
        mother_bmi_admission                NUMERIC(6,2) NULL,
        mother_muac_cm_admission            NUMERIC(6,2) NULL,
        mother_hemoglobin_admission         NUMERIC(6,2) NULL,
        mother_neck_circumference_cm_admission NUMERIC(6,2) NULL,
        mother_gravida                      INT NULL,
        mother_para                         INT NULL,
        mother_blood_sugar_fasting_admission NUMERIC(6,2) NULL,
        mother_blood_sugar_random_admission  NUMERIC(6,2) NULL,
        mother_bp_sys_admission             NUMERIC(6,2) NULL,
        mother_bp_dia_admission             NUMERIC(6,2) NULL,
        mother_growth_advice_admission      VARCHAR(200) NULL,

        -- ===== child_growth, admission-time slice (recorded_at_admission=1) =====
        admission_weight_kg          NUMERIC(6,2) NULL,
        admission_height_cm          NUMERIC(6,2) NULL,

        -- ===== admission_criteria, flattened (config.admission_criteria_values, 9 rows) =====
        ac_wfl_wfh_lt3z              BIT NOT NULL DEFAULT 0,  -- 1: WFL/WFH <-3Z
        ac_wfa_lt3z                  BIT NOT NULL DEFAULT 0,  -- 2: WFA <-3Z
        ac_muac_lt115                BIT NOT NULL DEFAULT 0,  -- 3: MUAC < 115mm
        ac_severe_wasting            BIT NOT NULL DEFAULT 0,  -- 4: Severe Wasting
        ac_bl_edema                  BIT NOT NULL DEFAULT 0,  -- 5: B/L Edema
        ac_others                    BIT NOT NULL DEFAULT 0,  -- 6: Others
        ac_others_value              VARCHAR(100) NULL,
        ac_muac_115_125              BIT NOT NULL DEFAULT 0,  -- 7: MUAC 115-125mm
        ac_wfl_wfh_lt2z              BIT NOT NULL DEFAULT 0,  -- 8: WFL/WFH <-2Z
        ac_wfl_wfh_lt1z              BIT NOT NULL DEFAULT 0,  -- 9: WFL/WFH <-1Z

        -- ===== admission_comorbid_condition, flattened (config.comorbid_condition_values, 12 rows) =====
        cc_pneumonia                 BIT NOT NULL DEFAULT 0,  -- 1
        cc_gastroenteritis           BIT NOT NULL DEFAULT 0,  -- 2
        cc_sepsis                    BIT NOT NULL DEFAULT 0,  -- 3
        cc_chd                       BIT NOT NULL DEFAULT 0,  -- 4
        cc_cp                        BIT NOT NULL DEFAULT 0,  -- 5
        cc_renal_disease             BIT NOT NULL DEFAULT 0,  -- 6
        cc_meningitis                BIT NOT NULL DEFAULT 0,  -- 7
        cc_ics                       BIT NOT NULL DEFAULT 0,  -- 8
        cc_iem                       BIT NOT NULL DEFAULT 0,  -- 9
        cc_tuberculosis              BIT NOT NULL DEFAULT 0,  -- 10
        cc_others                    BIT NOT NULL DEFAULT 0,  -- 11
        cc_others_value              VARCHAR(100) NULL,
        cc_covid19                   BIT NOT NULL DEFAULT 0,  -- 12

        -- ===== case_mother_social_factors, flattened (config.social_factors, 6 rows) =====
        sf_poverty                   BIT NOT NULL DEFAULT 0,  -- 1
        sf_ignorance                 BIT NOT NULL DEFAULT 0,  -- 2
        sf_illiteracy                BIT NOT NULL DEFAULT 0,  -- 3
        sf_migrants                  BIT NOT NULL DEFAULT 0,  -- 4
        sf_working_mother            BIT NOT NULL DEFAULT 0,  -- 5
        sf_others                    BIT NOT NULL DEFAULT 0,  -- 6
        sf_others_value              VARCHAR(100) NULL,

        -- ===== Denormalized "latest" summary columns (decision #2 - updated
        -- in place by weight-capture/discharge/followup submissions; full
        -- history lives in the small companion tables, FK'd to case_id) =====
        last_measured_weight         NUMERIC(6,2) NULL,
        last_measured_date           DATE NULL,
        last_discharge_date          DATETIME NULL,   -- set once Stage 4 ships
        last_followup_date           DATE NULL,       -- set once Stage 5 ships
        next_followup_date           DATE NULL,       -- set once Stage 5 ships

        -- form_status state machine (spec): 0=new admission, 1=first update,
        -- 3.x=weight-capture sequence (3, 3.1, 3.2, ...), 4.x=discharge
        -- (4, 4.1, 4.2, ...), 5.x=followup (5.1, 5.2, 5.3, ...).
        form_status                  DECIMAL(4,1) NOT NULL DEFAULT 0,

        -- ===== Legacy linkage - old-schema PKs this row was built from, kept
        -- so the migration is traceable/re-runnable and old tables can still
        -- be cross-referenced during the rollback-safety window =====
        legacy_case_id               INT NULL,
        legacy_child_id              INT NULL,
        legacy_mother_id             INT NULL,

        created_on                   DATETIME NOT NULL DEFAULT GETDATE(),
        created_by                   INT NULL,
        last_updated_on              DATETIME NULL,
        last_updated_by              INT NULL
    );

    CREATE INDEX ix_nrc_admission_h_id ON nr.nrc_admission(h_id);
    CREATE INDEX ix_nrc_admission_legacy_case_id ON nr.nrc_admission(legacy_case_id);
    CREATE INDEX ix_nrc_admission_form_status ON nr.nrc_admission(form_status);
    -- NOT unique: live case_details has 2,740 uh_id values that repeat
    -- across multiple cases (e.g. uh_id=405 appears 160 times) - almost
    -- certainly legacy data-entry debt (placeholder/typo UHIDs), not a
    -- constraint this migration gets to enforce retroactively without
    -- rejecting real historical rows. Plain (non-unique) index only.
    CREATE INDEX ix_nrc_admission_uh_id ON nr.nrc_admission(uh_id);
END
