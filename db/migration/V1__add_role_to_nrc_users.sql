-- Adds explicit role support to nr.nrc_users.
--
-- Today (verified against the live local nrc_live DB, 157 rows) every
-- account is undifferentiated: nrcadmin's POST /admin/login and nrcapp's
-- POST /login both authenticate against this exact same table with the
-- same password check, and there is no role/type column at all - every
-- row has h_id set, there's no separate "admin" table in use (the
-- cstep.nrcadmin.entity.Admin JPA class is dead code, no matching table
-- exists in nrc_live).
--
-- This migration introduces a real role so NRC_BACKEND's JWT can carry
-- ADMIN vs HOSPITAL_USER instead of granting every authenticated user
-- identical access. All existing users default to HOSPITAL_USER.
--
-- NOTE: like KABHI_BACKEND's db/migration files, Flyway is not wired up in
-- this project - run this manually against nrc_live before booting
-- NRC_BACKEND for the first time.

ALTER TABLE nr.nrc_users
    ADD role VARCHAR(20) NOT NULL CONSTRAINT DF_nrc_users_role DEFAULT 'HOSPITAL_USER';
GO

-- Promote the known admin account (per NRC_DOCS\All NRC Hospital
-- Credentials.xlsx, "Admin App" sheet: SNEHA NRC Admin App login):
UPDATE nr.nrc_users SET role = 'ADMIN' WHERE user_id = 'ddnutrition';
GO
