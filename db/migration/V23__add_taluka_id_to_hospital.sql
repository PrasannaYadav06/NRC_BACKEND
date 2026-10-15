-- Adds a nullable taluka_id link from nr.hospital to the new config.taluka
-- master data (V22), so the In-Patient Care District/Taluka filter can
-- cascade through a real hospital->taluka relationship. Additive, idempotent.
IF NOT EXISTS (
	SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('nr.hospital') AND name = 'taluka_id'
)
BEGIN
	ALTER TABLE nr.hospital ADD taluka_id INT NULL;
	ALTER TABLE nr.hospital ADD CONSTRAINT FK_hospital_taluka FOREIGN KEY (taluka_id) REFERENCES config.taluka(taluka_id);
END
GO

-- Backfill "where determinable": TALUKA-level hospitals in this DB are all
-- named literally "Taluk NRC <Taluka Name>" (confirmed against a live
-- sample - e.g. h_id 33 = "Taluk NRC Hungund", h_id 42 = "Taluk NRC
-- Doddaballapura") - matched within the same district to avoid cross-
-- district taluk-name collisions. DISTRICT-level hospitals have no single
-- taluka to backfill and are correctly left NULL.
UPDATE h
SET h.taluka_id = t.taluka_id
FROM nr.hospital h
JOIN config.taluka t ON t.district_id = h.district_id
	AND h.name = 'Taluk NRC ' + t.taluka_name
WHERE h.hospital_level = 'TALUKA' AND h.taluka_id IS NULL;
GO
