-- h_id=42 ("TNRC Doddaballapura") was registered under district_id=5
-- (Bengaluru Urban), but "Doddaballapura" taluka only exists under
-- district_id=4 (Bengaluru Rural) - a real conflict predating this project
-- (flagged during the original V23 backfill, never resolved). User
-- confirmed directly: Bengaluru Rural is correct. Reassigns district_id +
-- taluka_id, and regenerates h_code/username/password under district 4's
-- own sequence (next free slot there is 3) since the old ones encoded the
-- wrong district (username "tnrc53" = district 5, seq 3). name/hospital
-- level/taluka NAME are unchanged - only the district it's actually filed
-- under was wrong.
UPDATE nr.hospital SET district_id = 4, taluka_id = 20, h_code = 'TNRC-3', last_updated_on = GETDATE() WHERE h_id = 42;
UPDATE nr.nrc_users SET user_id = 'tnrc43', password = '$2b$12$2b/MyKUTffB81rkoZAm/BOQO5r9vSBUpBxJ0.vmRkfricQUrjKq9e', must_change_password = 1, last_updated_on = GETDATE() WHERE h_id = 42;
