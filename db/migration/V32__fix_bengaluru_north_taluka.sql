-- Same never-persisted-taluka_id bug as V31, for 2 more hospitals from the
-- same Phase 5 backfill batch (h_id 112/113, both correctly district_id=5,
-- matching their taluka's own district - safe to apply directly, no
-- conflict). h_id=42 ("TNRC Doddaballapura") is DELIBERATELY excluded here:
-- its district_id is 5 (Bengaluru Urban) but "Doddaballapura" taluka only
-- exists under district_id=4 (Bengaluru Rural) - a real, pre-existing data
-- conflict (already flagged during the original V23 backfill, before this
-- project even started) - needs a human decision, not a guess.

UPDATE nr.hospital SET taluka_id = 23, name = 'TNRC Bengaluru North - CV Raman Nagar', last_updated_on = GETDATE() WHERE h_id = 112;
UPDATE nr.hospital SET taluka_id = 24, name = 'TNRC Bengaluru North - Pulikeshinagar', last_updated_on = GETDATE() WHERE h_id = 113;
