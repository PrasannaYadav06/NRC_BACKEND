-- Follow-up to V27: Taluk NRC Hosadurga (h_id=147, Chitradurga) and Taluk
-- NRC Shiggaon (h_id=146, Haveri) were held back from the Phase 5
-- regeneration on a stale assumption - their taluks ("Hosadurga"/
-- "Shiggaon") were actually already added to config.taluka by V25 (Phase 1),
-- just not backfilled onto these two hospital rows. Re-checked live before
-- writing this file, not re-assumed. Backfills taluka_id, then applies the
-- same code/name/username/password regeneration rule as V27's 142 rows -
-- sequence numbers computed by hand (6 for Chitradurga, 5 for Haveri) since
-- each hospital's OWN old h_code ("TNRC-112"/"TNRC-111") coincidentally
-- matches the [DT]NRC-N pattern and would have inflated a MAX(...) query.
-- Not idempotent, same as V27 - run once.

UPDATE nr.hospital SET taluka_id = 140, h_code = 'TNRC-6', name = 'TNRC Hosadurga', last_updated_on = GETDATE() WHERE h_id = 147;
UPDATE nr.nrc_users SET user_id = 'tnrc106', password = '$2b$12$cZ4Ra1N4BqxzppWWxZfUmetXeZvsYwo655YIjzcX9BCtkzpUXOr4i', must_change_password = 1, last_updated_on = GETDATE() WHERE h_id = 147;

UPDATE nr.hospital SET taluka_id = 165, h_code = 'TNRC-5', name = 'TNRC Shiggaon', last_updated_on = GETDATE() WHERE h_id = 146;
UPDATE nr.nrc_users SET user_id = 'tnrc165', password = '$2b$12$qC2kLOM/2BnvFdeL2RqZWOsZqWMi4gu7D6jyuBnDBMDC24goPqOfS', must_change_password = 1, last_updated_on = GETDATE() WHERE h_id = 146;
