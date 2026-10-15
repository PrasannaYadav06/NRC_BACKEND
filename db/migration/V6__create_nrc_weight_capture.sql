-- Stage 3 - daily weight-capture history table (decision #2: the admission
-- parent row always carries the latest weight/date in place; this small
-- table feeds the View popup's full "1st capture / 2nd capture / ..." list).
-- Backfilled from nr.child_growth's daily rows (recorded_at_admission=0 AND
-- recorded_at_discharge=0) - nr.child_growth itself is left untouched.
--
-- Run against the LOCAL SQLEXPRESS nrc_live copy only, after explicit
-- review/approval - not yet executed as of writing this file. Run AFTER
-- V5__populate_nrc_admission.sql (needs nr.nrc_admission populated so the
-- legacy_case_id -> case_id remap below has rows to join against).

IF NOT EXISTS (SELECT 1 FROM sys.tables t JOIN sys.schemas s ON t.schema_id = s.schema_id WHERE s.name = 'nr' AND t.name = 'nrc_weight_capture')
BEGIN
    CREATE TABLE nr.nrc_weight_capture (
        capture_id      INT IDENTITY(1,1) PRIMARY KEY,
        case_id         INT NOT NULL,              -- FK -> nr.nrc_admission.case_id
        weight_kg       NUMERIC(6,2) NULL,
        diet            VARCHAR(50) NULL,
        comment         VARCHAR(100) NULL,
        capture_date    DATE NULL,
        legacy_rec_id   INT NULL,                  -- traceability back to nr.child_growth.rec_id
        created_on      DATETIME NOT NULL DEFAULT GETDATE(),
        created_by      INT NULL,
        CONSTRAINT fk_nrc_weight_capture_admission FOREIGN KEY (case_id) REFERENCES nr.nrc_admission(case_id)
    );

    CREATE INDEX ix_nrc_weight_capture_case_id ON nr.nrc_weight_capture(case_id);
END
GO

INSERT INTO nr.nrc_weight_capture (case_id, weight_kg, diet, comment, capture_date, legacy_rec_id, created_on, created_by)
SELECT na.case_id, cg.weight_kg, cg.diet, cg.daily_weight_comment, cg.measurement_date, cg.rec_id, cg.created_on, cg.created_by
FROM nr.child_growth cg
INNER JOIN nr.nrc_admission na ON na.legacy_case_id = cg.case_id
WHERE ISNULL(cg.recorded_at_admission, 0) = 0
  AND ISNULL(cg.recorded_at_discharge, 0) = 0
  AND NOT EXISTS (
      SELECT 1 FROM nr.nrc_weight_capture wc WHERE wc.legacy_rec_id = cg.rec_id
  );
