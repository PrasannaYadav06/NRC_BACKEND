-- Adds account-lockout + audit columns to nr.nrc_users, modeled on
-- KABHI_BACKEND's login hardening (KabhiUsersController: 5 failed attempts
-- -> 15 minute lock).
--
-- failed_attempts / lock_until: back AuthService's new lockout check.
-- last_login_ip / last_logout_on: minimal login/logout audit trail - KABHI
-- itself has none of this, added here anyway per the user's explicit VAPT
-- ask rather than skipped just because the reference app lacks it.
--
-- Idempotent: safe to re-run.

IF NOT EXISTS (
    SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = 'nr' AND TABLE_NAME = 'nrc_users' AND COLUMN_NAME = 'failed_attempts'
)
BEGIN
    ALTER TABLE nr.nrc_users ADD failed_attempts INT NOT NULL CONSTRAINT DF_nrc_users_failed_attempts DEFAULT 0;
END
GO

IF NOT EXISTS (
    SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = 'nr' AND TABLE_NAME = 'nrc_users' AND COLUMN_NAME = 'lock_until'
)
BEGIN
    ALTER TABLE nr.nrc_users ADD lock_until DATETIME NULL;
END
GO

IF NOT EXISTS (
    SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = 'nr' AND TABLE_NAME = 'nrc_users' AND COLUMN_NAME = 'last_login_ip'
)
BEGIN
    ALTER TABLE nr.nrc_users ADD last_login_ip VARCHAR(45) NULL;
END
GO

IF NOT EXISTS (
    SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = 'nr' AND TABLE_NAME = 'nrc_users' AND COLUMN_NAME = 'last_logout_on'
)
BEGIN
    ALTER TABLE nr.nrc_users ADD last_logout_on DATETIME NULL;
END
GO

-- Minimal login/logout audit log (append-only). Deliberately outside the
-- nr.nrc_users row itself so a lockout/audit query never competes with the
-- hot login-path UPDATE.
IF NOT EXISTS (
    SELECT 1 FROM INFORMATION_SCHEMA.TABLES
    WHERE TABLE_SCHEMA = 'nr' AND TABLE_NAME = 'nrc_login_audit'
)
BEGIN
    CREATE TABLE nr.nrc_login_audit (
        audit_id INT IDENTITY(1,1) PRIMARY KEY,
        u_id INT NULL,
        user_id VARCHAR(50) NULL,
        event_type VARCHAR(20) NOT NULL, -- LOGIN_SUCCESS / LOGIN_FAILURE / LOGOUT / LOCKOUT
        ip_address VARCHAR(45) NULL,
        event_on DATETIME NOT NULL CONSTRAINT DF_nrc_login_audit_event_on DEFAULT GETDATE()
    );
END
GO
