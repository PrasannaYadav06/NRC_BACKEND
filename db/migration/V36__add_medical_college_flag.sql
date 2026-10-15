-- Karnataka Facilities page: "Medical College" card. NRC's schema has no
-- Medical College concept at all before this (hospital_level is only
-- DISTRICT/TALUKA, confirmed by querying the live DB directly) - adds a
-- real flag instead of fabricating a card with a fake 0 count.
--
-- Backfill source: NRC_DOCS\All NRC Hospital Credentials.xlsx's "All
-- Districts" sheet (Long Name column) - verified live against nr.hospital
-- first, all 10 short names below match unchanged. Marked TRUE only for
-- hospitals whose Excel long name explicitly says "-DME" (Directorate of
-- Medical Education - the Karnataka state body overseeing government
-- medical colleges) or "-BMCRI" (Bangalore Medical College & Research
-- Institute, a named medical college):
--   VVH, BLCH (BMCRI-affiliated), BIMS, VIMS, BRIMS, KIMS, HIMS, MIMS,
--   RIMS, SIMS (all "<Place> Institute of Medical Sciences (...)-DME" -
--   real Karnataka government medical colleges).
-- Deliberately NOT marked: IGICH (Indira Gandhi Institute of Child
-- Health) - a standalone postgraduate paediatric institute, not an
-- MBBS-granting medical college, and its Excel long name says
-- "-Pediatric", not "-DME"/"-BMCRI" like the 10 above. Every other
-- DISTRICT hospital (plain "<Place> -District Hospital" / BRSH) stays
-- FALSE - confirmed none of their Excel long names mention a medical
-- college either.

IF NOT EXISTS (
	SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('nr.hospital') AND name = 'is_medical_college'
)
BEGIN
	ALTER TABLE nr.hospital ADD is_medical_college BIT NOT NULL DEFAULT 0;
END
GO

UPDATE nr.hospital SET is_medical_college = 1
WHERE name_short IN ('VVH', 'BLCH', 'BIMS', 'VIMS', 'BRIMS', 'KIMS', 'HIMS', 'MIMS', 'RIMS', 'SIMS')
AND is_medical_college = 0;
GO
