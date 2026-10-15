-- config.taluka had 4 rows that are actually PHC/CHC facility names, not
-- real taluks (pre-existing NRC data quality issue, predates this project -
-- confirmed via `taluka_name LIKE '%CHC%' OR '%PHC%'`, not guessed):
--   39  BelaguruCHC   (Chitradurga) - real facility is "Belaguru" CHC,
--                     already in config.chc (chc_id=76) under the correct
--                     real taluka Hosadurga (taluka_id=140).
--   78  Matamari (PHC) (Raichur) - real facility already in config.phc
--                     (phc_id=1817) under the correct real taluka Raichur
--                     (taluka_id=181, the district's own HQ taluk).
--   89  HolaluruCHC   (Shivamogga) - no hospital references it; "Holaluru"
--                     CHC already exists correctly in config.chc separately.
--   90  AyanuruCHC    (Shivamogga) - no hospital references it; "Ayanuru"
--                     isn't in the DMHPv1 source CHC list under any
--                     district/taluka - not fabricated, left unresolved.
--
-- 2 real hospitals (h_id 51, 140) were registered against the bad rows
-- before this fix existed - migrated to the correct taluka_id + chc_id/
-- phc_id, name updated to match the real PHC/CHC naming convention
-- ("TNRC {name}(CHC)"/"(PHC)", same rule generateIdentity() uses for new
-- registrations). h_code is untouched - this is a categorization fix, not
-- a renumbering.
--
-- Idempotent-ish: safe to re-run (hospital UPDATEs are no-ops the 2nd time;
-- the taluka DELETEs are naturally idempotent via IF EXISTS).

UPDATE nr.hospital SET taluka_id = 140, chc_id = 76, name = 'TNRC Belaguru(CHC)', last_updated_on = GETDATE() WHERE h_id = 51;
UPDATE nr.hospital SET taluka_id = 181, phc_id = 1817, name = 'TNRC Matamari(PHC)', last_updated_on = GETDATE() WHERE h_id = 140;

IF EXISTS (SELECT 1 FROM config.taluka WHERE taluka_id = 39) DELETE FROM config.taluka WHERE taluka_id = 39;
IF EXISTS (SELECT 1 FROM config.taluka WHERE taluka_id = 78) DELETE FROM config.taluka WHERE taluka_id = 78;
IF EXISTS (SELECT 1 FROM config.taluka WHERE taluka_id = 89) DELETE FROM config.taluka WHERE taluka_id = 89;
IF EXISTS (SELECT 1 FROM config.taluka WHERE taluka_id = 90) DELETE FROM config.taluka WHERE taluka_id = 90;
