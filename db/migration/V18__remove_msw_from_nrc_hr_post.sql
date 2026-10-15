-- Removes MSW from the KABHI-style HR approval-workflow tables (nr.nrc_hr_post
-- / nr.nrc_hr_sanctioned), per explicit request to drop it from the
-- Designation dropdown. Verified before writing this: zero nr.nrc_hr_post
-- rows exist for MSW (nobody ever submitted one) - the 145
-- nr.nrc_hr_sanctioned rows are auto-seeded defaults (sanctioned_count=1),
-- created automatically by NrcHrPostService#getSanctionedCount the first
-- time each hospital's cumulative/matrix table was loaded this session, not
-- real submitted data. Safe to delete outright.
--
-- Does NOT touch nr.nrc_hr (the old counts-only aggregate table, V15/V16) -
-- its msw_sanctioned/msw_working/msw_vacant/msw_training columns hold real
-- historical data going back to 2020 and stay as a frozen historical
-- snapshot, same as every other superseded table in this project.
--
-- Run against the LOCAL SQLEXPRESS nrc_live copy only, after explicit
-- review/approval - not yet executed as of writing this file.

DELETE FROM nr.nrc_hr_post WHERE designation = 'MSW';
DELETE FROM nr.nrc_hr_sanctioned WHERE designation = 'MSW';
