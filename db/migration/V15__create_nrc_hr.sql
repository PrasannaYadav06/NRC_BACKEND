-- Stage 6 - HR module consolidation.
-- Additive only: creates ONE new table, one row per (hospital, month, year)
-- HR submission, replacing nr.hospital_details' narrow EAV shape (one row
-- per hospital/month/hr_type_id, ~30 rows per submission) with KABHI's
-- flattened-columns convention (one form = one row) - same approach already
-- used for nr.nrc_admission's comorbid/criteria/social-factor flattening.
-- nr.hospital_details is left untouched, not dropped, and stops receiving
-- new writes once this ships (same cutover pattern as case_details in
-- Stage 3 - "nothing is lost by switching over", historical data stays put
-- as a rollback net).
--
-- Designations: the existing admin.component.ts HR form has always listed
-- SIX categories (Paediatrician/Medical Officer, Staff Nurse, Dietician,
-- Cook, Attender, MSW), and hospital_details actually has real MSW history
-- (hr_type_id 21-24, same sanctioned/working/vacant/training %4 pattern as
-- the other five, 220 rows going back to 2020) - but the CURRENTLY-LIVE
-- frontend/ResourceService path only ever wires up the first FIVE
-- (hr_type_id 1-20): admin.component.ts's template references
-- resoucelist.mswList[0] but onSubmithrForm never builds or sends an
-- mswList, so MSW has silently stopped saving at some point since that
-- 2020 data was written. Wiring MSW back up here (msw_sanctioned/working/
-- vacant/training columns, backfilled from hr_type_id 21-24 in V16) rather
-- than porting the current gap forward, per "keeping NRC's own
-- designations (from the existing admin form)" - MSW was always meant to
-- be one of them and has real history worth carrying over.
--
-- Run against the LOCAL SQLEXPRESS nrc_live copy only, after explicit
-- review/approval - not yet executed as of writing this file.

IF NOT EXISTS (SELECT 1 FROM sys.tables t JOIN sys.schemas s ON t.schema_id = s.schema_id WHERE s.name = 'nr' AND t.name = 'nrc_hr')
BEGIN
    CREATE TABLE nr.nrc_hr (
        hr_rec_id           INT IDENTITY(1,1) PRIMARY KEY,
        h_id                INT NOT NULL,  -- FK -> nr.hospital.h_id
        month               INT NOT NULL,
        year                INT NOT NULL,

        number_of_beds      INT NULL,

        officer_sanctioned  INT NULL, officer_working  INT NULL, officer_vacant  INT NULL, officer_training  INT NULL,
        nurse_sanctioned    INT NULL, nurse_working    INT NULL, nurse_vacant    INT NULL, nurse_training    INT NULL,
        dietician_sanctioned INT NULL, dietician_working INT NULL, dietician_vacant INT NULL, dietician_training INT NULL,
        cook_sanctioned     INT NULL, cook_working     INT NULL, cook_vacant     INT NULL, cook_training     INT NULL,
        tender_sanctioned   INT NULL, tender_working   INT NULL, tender_vacant   INT NULL, tender_training   INT NULL,
        msw_sanctioned      INT NULL, msw_working      INT NULL, msw_vacant      INT NULL, msw_training      INT NULL,

        -- legacy linkage (informational only, not a real FK - the old
        -- table's rows don't collapse 1:1 into this one, see V16)
        legacy_details_ids  VARCHAR(200) NULL,

        created_on          DATETIME NULL,
        created_by          INT NULL,
        last_updated_on     DATETIME NULL,
        last_updated_by     INT NULL,

        CONSTRAINT fk_nrc_hr_hospital FOREIGN KEY (h_id) REFERENCES nr.hospital(h_id)
    );

    CREATE INDEX ix_nrc_hr_hospital_month_year ON nr.nrc_hr (h_id, year, month);
END
