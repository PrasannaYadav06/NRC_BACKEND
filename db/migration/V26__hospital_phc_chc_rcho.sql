-- NRC Hospital Registration redesign + RCHO role, Phase 2 (additive schema).
-- See plan: NRC Hospital Registration redesign + RCHO role + 2-tier HR approval.
-- Idempotent: safe to re-run. Run against local SQLEXPRESS nrc_live only.

-- nr.hospital: link to the specific PHC/CHC a hospital was registered under
-- (mutually exclusive, enforced in service code, not a DB constraint - a
-- hospital can also be DISTRICT/pure-TALUKA level with both null).
IF NOT EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('nr.hospital') AND name = 'phc_id')
    ALTER TABLE nr.hospital ADD phc_id INT NULL CONSTRAINT fk_hospital_phc FOREIGN KEY REFERENCES config.phc(phc_id);

IF NOT EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('nr.hospital') AND name = 'chc_id')
    ALTER TABLE nr.hospital ADD chc_id INT NULL CONSTRAINT fk_hospital_chc FOREIGN KEY REFERENCES config.chc(chc_id);

-- nr.rcho: one row per district. RCHO's hospital scope is derived by joining
-- nr.hospital.district_id = nr.rcho.district_id (not a separate hospital-link
-- column - every hospital already carries its own district_id).
IF NOT EXISTS (SELECT 1 FROM sys.tables t JOIN sys.schemas s ON t.schema_id = s.schema_id WHERE s.name = 'nr' AND t.name = 'rcho')
BEGIN
    CREATE TABLE nr.rcho (
        rcho_id         INT IDENTITY(1,1) PRIMARY KEY,
        district_id     INT NOT NULL,
        u_id            INT NOT NULL,
        rcho_name       VARCHAR(150) NOT NULL,
        created_on      DATETIME NULL,
        last_updated_on DATETIME NULL,
        CONSTRAINT fk_rcho_district FOREIGN KEY (district_id) REFERENCES config.district(district_id),
        CONSTRAINT fk_rcho_user FOREIGN KEY (u_id) REFERENCES nr.nrc_users(u_id),
        CONSTRAINT uq_rcho_district UNIQUE (district_id),
        CONSTRAINT uq_rcho_user UNIQUE (u_id)
    );
END

-- nr.nrc_hr_post: add the RCHO review tier ahead of the existing admin tier.
-- approval_status flow: PENDING -> (RCHO acts) -> RCHO_APPROVED -> (Admin acts)
-- -> APPROVED, with REJECTED reachable (terminal) from either tier - the
-- rcho_* vs existing approved_by/approved_date/remarks columns record which
-- tier actually acted. is_current still only flips true on final admin APPROVED.
IF NOT EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('nr.nrc_hr_post') AND name = 'rcho_approved_by')
    ALTER TABLE nr.nrc_hr_post ADD rcho_approved_by VARCHAR(50) NULL;

IF NOT EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('nr.nrc_hr_post') AND name = 'rcho_approved_date')
    ALTER TABLE nr.nrc_hr_post ADD rcho_approved_date DATETIME NULL;

IF NOT EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('nr.nrc_hr_post') AND name = 'rcho_remarks')
    ALTER TABLE nr.nrc_hr_post ADD rcho_remarks VARCHAR(300) NULL;
