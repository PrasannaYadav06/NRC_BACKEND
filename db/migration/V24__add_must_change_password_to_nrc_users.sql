-- Adds must_change_password to nr.nrc_users.
--
-- Drives the "kindly change the password" banner shown on every page from
-- first login until the user changes it (see AuthService#login/
-- #changePassword). Defaults to 0 (false) so all existing accounts are
-- unaffected - only NEW hospital accounts (see HospitalAdminService
-- #saveUser) get created with it set to 1.
--
-- Idempotent: safe to re-run.

IF NOT EXISTS (
    SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = 'nr' AND TABLE_NAME = 'nrc_users' AND COLUMN_NAME = 'must_change_password'
)
BEGIN
    ALTER TABLE nr.nrc_users ADD must_change_password BIT NOT NULL DEFAULT 0;
END
GO
