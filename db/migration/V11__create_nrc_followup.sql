-- Stage 5 - Follow-up module consolidation.
-- Additive only: creates ONE new table consolidating nr.followup (actual
-- completed visits, 68,157 rows) and nr.followup_schedule (the due-date
-- schedule, 251,872 rows) - both left untouched, not dropped.
-- config.followup_config/config.followup_place stay as-is (reference
-- lookup tables, same pattern as config.comorbid_condition_values etc -
-- referenced by id, not migrated).
--
-- Why NOT a row-by-row merge: checked whether an actual visit's date
-- reliably matches its originally-scheduled date (which would let one row
-- represent "this scheduled follow-up, completed on this date") - only
-- 27,727 of 68,157 actual visits (41%) match a scheduled date exactly.
-- Patients don't always show up on the scheduled day, and there's no other
-- FK between the two legacy tables (followup_schedule.followup_id is a
-- local per-case sequence number 1/2/3/4..., not a real foreign key to
-- nr.followup.followup_id). Forcing a date-match merge would misrepresent
-- ~59% of real visit history. Instead both sources land in this ONE table
-- as clearly-typed rows (record_type = 'VISIT' or 'SCHEDULE'), preserving
-- full fidelity of both - genuinely "one table", without losing or
-- guessing at data the old schema itself never linked cleanly.
--
-- Run against the LOCAL SQLEXPRESS nrc_live copy only, after explicit
-- review/approval - not yet executed as of writing this file.

IF NOT EXISTS (SELECT 1 FROM sys.tables t JOIN sys.schemas s ON t.schema_id = s.schema_id WHERE s.name = 'nr' AND t.name = 'nrc_followup')
BEGIN
    CREATE TABLE nr.nrc_followup (
        followup_rec_id     INT IDENTITY(1,1) PRIMARY KEY,
        case_id             INT NOT NULL,  -- FK -> nr.nrc_admission.case_id

        record_type         VARCHAR(10) NOT NULL,  -- 'VISIT' (completed) or 'SCHEDULE' (due date)
        followup_seq        INT NULL,               -- 1st/2nd/3rd/... (SCHEDULE rows only, from legacy followup_schedule.followup_id)
        followup_date       DATE NULL,              -- visit date (VISIT) or due date (SCHEDULE)

        -- VISIT-only fields (from nr.followup)
        place_id            INT NULL,
        status_id           INT NULL,
        followup_sd         NUMERIC(6,2) NULL,
        done_by             VARCHAR(30) NULL,
        remarks             VARCHAR(100) NULL,

        -- SCHEDULE-only field (from nr.followup_schedule)
        is_active           BIT NULL,

        -- form_status contribution: 5.1/5.2/5.3... incrementing per VISIT
        -- (state machine lives on nr.nrc_admission.form_status; this column
        -- just records what this particular visit set it to).
        form_status_at_event DECIMAL(4,1) NULL,

        legacy_followup_id  INT NULL,   -- nr.followup.followup_id (VISIT rows)
        legacy_sched_id     INT NULL,   -- nr.followup_schedule.sched_id (SCHEDULE rows)
        legacy_case_id      INT NULL,

        created_on          DATETIME NOT NULL DEFAULT GETDATE(),
        created_by          INT NULL,
        last_updated_on     DATETIME NULL,
        last_updated_by     INT NULL,

        CONSTRAINT fk_nrc_followup_admission FOREIGN KEY (case_id) REFERENCES nr.nrc_admission(case_id)
    );

    CREATE INDEX ix_nrc_followup_case_id ON nr.nrc_followup(case_id);
    CREATE INDEX ix_nrc_followup_legacy_case_id ON nr.nrc_followup(legacy_case_id);
    CREATE INDEX ix_nrc_followup_record_type ON nr.nrc_followup(record_type);
END
