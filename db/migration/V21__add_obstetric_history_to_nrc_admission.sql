-- Adds a free-text Obstetric History field to nr.nrc_admission, replacing
-- the admission form's Gravida/Para/No. of children fields. Those columns
-- (mother_gravida, mother_para, no_of_children) are left in place - not
-- dropped - so historical data and any existing report/query against them
-- keeps working; the admission form simply stops populating them going
-- forward. Additive only, idempotent - safe to re-run.
IF NOT EXISTS (
    SELECT 1 FROM sys.columns
    WHERE object_id = OBJECT_ID('nr.nrc_admission') AND name = 'obstetric_history'
)
BEGIN
    ALTER TABLE nr.nrc_admission ADD obstetric_history NVARCHAR(1000) NULL;
END
GO
