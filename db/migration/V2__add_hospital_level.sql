-- Adds hospital_level (DISTRICT / TALUKA) to nr.hospital.
--
-- Needed for the restructured dashboard's per-role card breakdown (admin
-- sees District-total + Taluka-total; a district/taluka user sees their own
-- single number). No such classification exists today - nr.hospital has no
-- level/type column at all.
--
-- Source of truth: D:\Projects\FRONTEND\NRC_FRONTEND\NRC_DOCS\
-- All NRC Hospital Credentials.xlsx - "All Districts" sheet (33 hospitals)
-- and "All Taluks" sheet (110 taluks). Matched via each sheet's login
-- username column against nr.nrc_users.user_id -> h_id -> nr.hospital,
-- NOT by name (hospital names/short-names have inconsistent casing/spacing
-- vs the spreadsheet; usernames are exact and already live in the DB).
-- Verified against the live local nrc_live copy before writing this:
-- all 33 district usernames and all 110 taluk usernames matched exactly
-- one hospital each (143 of 153 total hospitals). The 10 unmatched are:
--   - 8 obvious test/demo hospitals added after the spreadsheet's
--     snapshot (h_id 145, 148-151, 1150-1152: "NRC Demo", "Test Hospital
--     Name", "NRC TESTING" x4, etc) - left as NULL hospital_level, not a
--     real classification gap.
--   - 2 real taluk hospitals (h_id 146 "TNRC Shiggaon", h_id 147
--     "TNRC Hosadurga") added after the spreadsheet's "Added in June 2022"
--     batch - not in the username list, but their h_code follows the exact
--     "TNRC-nnn" pattern every other real taluk hospital uses, so matched
--     by that pattern instead as a safe fallback.
--
-- Idempotent: safe to re-run.

IF NOT EXISTS (
    SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = 'nr' AND TABLE_NAME = 'hospital' AND COLUMN_NAME = 'hospital_level'
)
BEGIN
    ALTER TABLE nr.hospital ADD hospital_level VARCHAR(10) NULL;
END
GO

UPDATE nr.hospital SET hospital_level = 'DISTRICT'
WHERE h_id IN (
    SELECT u.h_id FROM nr.nrc_users u WHERE u.user_id IN (
        'nrc1bengaluruurban','nrc2bengaluruurban','nrc3bengaluruurban','nrc4belgaum','nrc5ballari',
        'nrc6bidar','nrc7dharwad','nrc8hassan','nrc9mandya','nrc10raichur','nrc11shimoga','nrc12bagalkot',
        'nrc13bijapur','nrc14chamarajanagar','nrc15chikballapur','nrc16chikkamagaluru','nrc17chitradurga',
        'nrc18davanagere','nrc19dharwad','nrc20dakshinakannada','nrc21gadag','nrc22kalaburagi','nrc23haveri',
        'nrc24kodagu','nrc25kolar','nrc26koppal','nrc27mysore','nrc28ramanagara','nrc29tumakuru',
        'nrc30udupi ','nrc31uttarakannada','nrc32yadgir','nrc33udupi2'
    )
);
GO

UPDATE nr.hospital SET hospital_level = 'TALUKA'
WHERE h_id IN (
    SELECT u.h_id FROM nr.nrc_users u WHERE u.user_id IN (
        'tnrc55badami','tnrc1hungund','tnrc2jamakhandi','tnrc3athani','tnrc4chikkodi','tnrc5gokak',
        'tnrc6raibhag','tnrc7saundatti','tnrc8hadagali','tnrc56harapanahalli','tnrc9hospet',
        'tnrc10doddaballapura','tnrc11aurad','tnrc12balki','tnrc57basavakalyana','tnrc13humnabad',
        'tnrc14gundlupet','tnrc58kollegala','tnrc15bagepalli','tnrc16chintamani','tnrc50gowribidanur',
        'tnrc17kadur','tnrc18tarikere','tnrc19belaguruchc','tnrc20challakere','tnrc21harappanahalli',
        'tnrc22navalagunda','tnrc23naragund','tnrc24ron','tnrc25arsikere','tnrc26belur','tnrc27byadagi',
        'tnrc28hirekeruru','tnrc29ranebennur','tnrc59afzalpur','tnrc60aland','tnrc30chincholi',
        'tnrc31chittapur','tnrc61jevargi','tnrc32sedam','tnrc62somwarpet','tnrc63virajpet','tnrc33bangarpet',
        'tnrc34malur','tnrc35mulbagal','tnrc77kgf','tnrc36gangavathi','tnrc37kustagi','tnrc38malavalli',
        'tnrc39hdkote','tnrc40lingsagur','tnrc41kanakapura','tnrc64soluru','tnrc65bhadravathi','tnrc42sagara',
        'tnrc66shikaripura','tnrc67soraba','tnrc68thirthahalli','tnrc51koratagere','tnrc52pavagada',
        'tnrc53sira','tnrc54tiptur','tnrc43karkala','tnrc69ankola','tnrc70bhatkal','tnrc71haliyal',
        'tnrc72honnavar','tnrc73joida','tnrc44kumta','tnrc45mundgod','tnrc74siddapur','tnrc75sirsi',
        'tnrc76yellapur','tnrc46bagewadi','tnrc47indi','tnrc48sindagi','tnrc49shorapura','tnrc78cvramannagar',
        'tnrc79pulikeshinagar','tnrc80anekal','tnrc81mahadevapura','tnrc82devanahalli','tnrc83nelamangala',
        'tnrc84holalkere','tnrc85molakalmuru','tnrc86mayakonda','tnrc87jagaluru','tnrc88holaluruchc',
        'tnrc89ayanuruchc','tnrc90mudigere','tnrc91sullia','tnrc92sakleshpur','tnrc93tnarasipura',
        'tnrc94nanjangudu','tnrc95kudachi','tnrc96yemkanmardi','tnrc97mudhol','tnrc98chadchan',
        'tnrc99shirahatti','tnrc100hagaribommanahalli','tnrc101siruguppa','tnrc102kudligi','tnrc103kampli',
        'tnrc104sanduru','tnrc105kalaburgirural','tnrc106matamariphc','tnrc107devadurga','tnrc108manvi',
        'tnrc109maski','tnrc110kanakagiri'
    )
    OR h_code LIKE 'TNRC-%'
);
GO
