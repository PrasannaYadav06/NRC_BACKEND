-- Real bug in this project's own Phase 5 work: h_id 122 ("TNRC HolaluruCHC")
-- and 123 ("TNRC AyanuruCHC") had their names computed from a taluka
-- backfill mapping used only for the dry-run report, but that mapping was
-- never actually written back to nr.hospital.taluka_id - both are still
-- NULL. User confirmed both belong under Shivamogga taluka (taluka_id=187).
-- "Holaluru" already exists in config.chc (chc_id=176) under this taluka;
-- "Ayanuru" doesn't exist anywhere in config.chc yet (confirmed absent from
-- the DMHPv1 source data too) - added here on the user's direct
-- confirmation, not guessed.

IF NOT EXISTS (SELECT 1 FROM config.chc WHERE chc_name='Ayanuru' AND taluka_id=187)
    INSERT INTO config.chc (chc_name, taluka_id, district_id, is_active) VALUES ('Ayanuru', 187, 25, 1);

UPDATE nr.hospital SET taluka_id = 187, chc_id = 176, name = 'TNRC Holaluru(CHC)', last_updated_on = GETDATE() WHERE h_id = 122;
UPDATE nr.hospital SET taluka_id = 187,
    chc_id = (SELECT chc_id FROM config.chc WHERE chc_name='Ayanuru' AND taluka_id=187),
    name = 'TNRC Ayanuru(CHC)', last_updated_on = GETDATE()
WHERE h_id = 123;
