-- Adds a soft is_active flag to config.admission_type so "Relapse" can be
-- hidden from the Type of Admission dropdown without deleting the row -
-- historical nrc_admission/case_details rows may still reference its
-- admission_type_id, and CaseQueryService's legacy RELAPSE=3 classification
-- constant (unrouted /createcase search path) also depends on the row
-- existing. Additive only, idempotent - safe to re-run.
IF NOT EXISTS (
    SELECT 1 FROM sys.columns
    WHERE object_id = OBJECT_ID('config.admission_type') AND name = 'is_active'
)
BEGIN
    ALTER TABLE config.admission_type ADD is_active BIT NOT NULL DEFAULT 1;
END
GO

UPDATE config.admission_type
SET is_active = 0
WHERE LOWER(LTRIM(RTRIM(admission_type))) = 'relapse';
GO
