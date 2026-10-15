-- Stage 6 (revised) - HR module rebuilt to match KABHI's actual HR module
-- (tbl_kabhi_hr / tbl_kabhi_hr_sanctioned / KabhiHrService) instead of the
-- simple monthly aggregate-count form this stage originally shipped with
-- (nr.nrc_hr, V15/V16 - left untouched, additive, now a frozen historical
-- snapshot of the old counts-only self-reported data; nothing built on it
-- is dropped).
--
-- Model, ported from KABHI's tbl_kabhi_hr: one row per HR "post slot"
-- submission (a hire, or a later vacate/status-change against that same
-- slot). The *current* state of a post is the latest row for the same
-- (h_id, designation, slot_no) that has approval_status = APPROVED and
-- is_current = 1; every other row for that slot is retained as history. A
-- row with approval_status = PENDING is an in-flight submission awaiting
-- admin approval and does not affect the post's current state yet - see
-- NrcHrPostService for the full state machine (faithfully ported from
-- KabhiHrService).
--
-- Deviates from KABHI in one place: KABHI's DISTRICT scope has one
-- sanctioned-count-per-designation that applies uniformly to every
-- district (districts are uniformly staffed by design). NRC hospitals are
-- NOT uniform (district vs taluka level, very different bed counts) and
-- the old self-reported form already had each hospital enter its own
-- sanctioned number - so nr.nrc_hr_sanctioned below is scoped per (h_id,
-- designation), not globally per designation. Everything else (the hire/
-- vacate/approve state machine, PENDING/APPROVED/REJECTED, is_current)
-- mirrors KABHI as-is.
--
-- Run against the LOCAL SQLEXPRESS nrc_live copy only, after explicit
-- review/approval - not yet executed as of writing this file.

IF NOT EXISTS (SELECT 1 FROM sys.tables t JOIN sys.schemas s ON t.schema_id = s.schema_id WHERE s.name = 'nr' AND t.name = 'nrc_hr_post')
BEGIN
    CREATE TABLE nr.nrc_hr_post (
        hr_post_id           INT IDENTITY(1,1) PRIMARY KEY,
        h_id                 INT NOT NULL,           -- FK -> nr.hospital.h_id

        designation          VARCHAR(60) NOT NULL,   -- Paediatrician/Medical Officer, Staff Nurse, Dietician, Cook, Attender, MSW
        slot_no              INT NOT NULL,           -- 1-based slot within the designation's sanctioned count

        person_name          VARCHAR(100) NULL,
        date_of_joining      DATE NULL,
        email_id             VARCHAR(100) NULL,
        contact_no           VARCHAR(20) NULL,
        employment_status    VARCHAR(30) NOT NULL,   -- Working / Vacant / Fulltime / On-Consultation / On-Deputation Basis / Rotational Duty Basis / Training
        days_of_consultation VARCHAR(100) NULL,       -- comma-separated day names, only when employment_status = On-Consultation

        approval_status      VARCHAR(15) NOT NULL DEFAULT 'PENDING',  -- PENDING / APPROVED / REJECTED
        is_current           BIT NOT NULL DEFAULT 0,  -- true only for the single row that represents the slot's live/approved state

        submitted_by         VARCHAR(50) NULL,
        submitted_date       DATETIME NULL,
        approved_by          VARCHAR(50) NULL,
        approved_date        DATETIME NULL,
        remarks              VARCHAR(300) NULL,

        CONSTRAINT fk_nrc_hr_post_hospital FOREIGN KEY (h_id) REFERENCES nr.hospital(h_id)
    );

    CREATE INDEX ix_nrc_hr_post_hospital_designation ON nr.nrc_hr_post (h_id, designation, slot_no);
    CREATE INDEX ix_nrc_hr_post_approval_status ON nr.nrc_hr_post (approval_status);
END

IF NOT EXISTS (SELECT 1 FROM sys.tables t JOIN sys.schemas s ON t.schema_id = s.schema_id WHERE s.name = 'nr' AND t.name = 'nrc_hr_sanctioned')
BEGIN
    CREATE TABLE nr.nrc_hr_sanctioned (
        hr_sanctioned_id     INT IDENTITY(1,1) PRIMARY KEY,
        h_id                 INT NOT NULL,           -- FK -> nr.hospital.h_id
        designation          VARCHAR(60) NOT NULL,
        sanctioned_count     INT NOT NULL DEFAULT 1,
        updated_by           VARCHAR(50) NULL,
        updated_date         DATETIME NULL,

        CONSTRAINT fk_nrc_hr_sanctioned_hospital FOREIGN KEY (h_id) REFERENCES nr.hospital(h_id),
        CONSTRAINT uq_nrc_hr_sanctioned UNIQUE (h_id, designation)
    );
END
