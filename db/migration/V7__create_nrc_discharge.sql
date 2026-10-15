-- Stage 4 - Discharge module consolidation.
-- Additive only: creates ONE new table consolidating the 3 tables listed
-- below (all left untouched, not dropped) plus discharge_date/discharge_sd/
-- discharged_by_doctor which currently sit on nr.case_details. One form =
-- one table, matching KABHI's convention (flattened multi-select
-- comorbid-condition checkboxes -> individual boolean columns, same
-- config.comorbid_condition_values master list Stage 3's nrc_admission
-- already uses).
--
-- Source tables consolidated here (row counts as of 2026-09-15, local copy):
--   discharge_child_details (62,970), discharge_comorbid_condition (19,543),
--   discharge_mother_details (11,457), plus the discharge_date/discharge_sd/
--   discharged_by_doctor columns of nr.case_details (62,955 populated).
--
-- Unlike nrc_admission (one row per case), this is a HISTORY table - one
-- row per discharge EVENT, FK'd to nrc_admission.case_id - because live
-- data already contains cases discharged/re-submitted more than once (see
-- decision #2's pattern, same as nrc_weight_capture). nr.nrc_admission's
-- own last_discharge_date column (added in Stage 3, unpopulated until now)
-- is updated to the latest event per case by V8.
--
-- Run against the LOCAL SQLEXPRESS nrc_live copy only, after explicit
-- review/approval - not yet executed as of writing this file.

IF NOT EXISTS (SELECT 1 FROM sys.tables t JOIN sys.schemas s ON t.schema_id = s.schema_id WHERE s.name = 'nr' AND t.name = 'nrc_discharge')
BEGIN
    CREATE TABLE nr.nrc_discharge (
        discharge_rec_id            INT IDENTITY(1,1) PRIMARY KEY,
        case_id                     INT NOT NULL,  -- FK -> nr.nrc_admission.case_id

        discharge_date               DATETIME NULL,
        discharge_sd                 NUMERIC(6,2) NULL,
        discharged_by_doctor         INT NULL,

        avg_weight_gain_kg           NUMERIC(6,2) NULL,
        clinical_impression          VARCHAR(10) NULL,  -- "Primary" / "Secondary"
        outcome_id                   INT NULL,
        treatment_given               VARCHAR(500) NULL,
        medical_advice                VARCHAR(500) NULL,
        dietary_advice                 VARCHAR(500) NULL,

        medical_risk_id              INT NULL,

        -- Comorbid conditions, flattened (config.comorbid_condition_values,
        -- 12 rows - same master list as nr.nrc_admission's ac_*/cc_* columns).
        cc_pneumonia                  BIT NOT NULL DEFAULT 0,
        cc_gastroenteritis            BIT NOT NULL DEFAULT 0,
        cc_sepsis                     BIT NOT NULL DEFAULT 0,
        cc_chd                        BIT NOT NULL DEFAULT 0,
        cc_cp                         BIT NOT NULL DEFAULT 0,
        cc_renal_disease              BIT NOT NULL DEFAULT 0,
        cc_meningitis                 BIT NOT NULL DEFAULT 0,
        cc_ics                        BIT NOT NULL DEFAULT 0,
        cc_iem                        BIT NOT NULL DEFAULT 0,
        cc_tuberculosis               BIT NOT NULL DEFAULT 0,
        cc_others                     BIT NOT NULL DEFAULT 0,
        cc_others_value               VARCHAR(100) NULL,
        cc_covid19                    BIT NOT NULL DEFAULT 0,

        -- form_status contribution: 4=first discharge, 4.1/4.2/...=edits
        -- (state machine lives on nr.nrc_admission.form_status; this column
        -- just records what this particular event set it to).
        form_status_at_event          DECIMAL(4,1) NULL,

        -- Legacy linkage - traceable back to the old-schema rows this event
        -- was built from.
        legacy_case_id                INT NULL,
        legacy_discharge_child_id     INT NULL,
        legacy_discharge_mother_id    INT NULL,

        created_on                    DATETIME NOT NULL DEFAULT GETDATE(),
        created_by                    INT NULL,
        last_updated_on               DATETIME NULL,
        last_updated_by               INT NULL,

        CONSTRAINT fk_nrc_discharge_admission FOREIGN KEY (case_id) REFERENCES nr.nrc_admission(case_id)
    );

    CREATE INDEX ix_nrc_discharge_case_id ON nr.nrc_discharge(case_id);
    CREATE INDEX ix_nrc_discharge_legacy_case_id ON nr.nrc_discharge(legacy_case_id);
END
