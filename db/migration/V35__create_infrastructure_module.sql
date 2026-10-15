-- Infrastructure Module scaffold (generic - Department's own reporting
-- format/guidelines not yet shared; built with flexible fields now so this
-- is extendable once they arrive, rather than waiting). One infrastructure
-- record per hospital (nr.nrc_infrastructure), plus a photo gallery table
-- (nr.nrc_infrastructure_photo, many photos per hospital) for visual
-- documentation across districts. Idempotent, same pattern as every other
-- migration in this project - safe to re-run.

IF NOT EXISTS (
	SELECT 1 FROM sys.tables WHERE schema_id = SCHEMA_ID('nr') AND name = 'nrc_infrastructure'
)
BEGIN
	CREATE TABLE nr.nrc_infrastructure (
		infra_id INT IDENTITY(1,1) PRIMARY KEY,
		h_id INT NOT NULL UNIQUE,
		details NVARCHAR(MAX) NULL,
		last_updated_on DATETIME NULL,
		last_updated_by INT NULL,
		CONSTRAINT FK_nrc_infrastructure_hospital FOREIGN KEY (h_id) REFERENCES nr.hospital(h_id)
	);
END
GO

IF NOT EXISTS (
	SELECT 1 FROM sys.tables WHERE schema_id = SCHEMA_ID('nr') AND name = 'nrc_infrastructure_photo'
)
BEGIN
	CREATE TABLE nr.nrc_infrastructure_photo (
		photo_id INT IDENTITY(1,1) PRIMARY KEY,
		h_id INT NOT NULL,
		file_path NVARCHAR(500) NOT NULL,
		caption NVARCHAR(300) NULL,
		uploaded_on DATETIME NOT NULL DEFAULT GETDATE(),
		uploaded_by INT NULL,
		CONSTRAINT FK_nrc_infrastructure_photo_hospital FOREIGN KEY (h_id) REFERENCES nr.hospital(h_id)
	);
END
GO
