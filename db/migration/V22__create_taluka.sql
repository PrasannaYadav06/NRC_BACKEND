-- New config.taluka master data (item 3 - District/Taluka filters on
-- In-Patient Care). No geographic taluka table existed anywhere in this
-- project before - only Hospital.districtId (the admin/HR page's own
-- "District/Taluka" toggle is a different concept, a hospital's own tier
-- from V2__add_hospital_level.sql, not a geographic filter).
--
-- Data sourced from NRC_DOCS\All NRC Hospital Credentials.xlsx's "All
-- Taluks" sheet (110 real rows once a stray "Added in June 2022" section
-- divider is dropped). That sheet's District column uses inconsistent
-- spellings for the same district (Bagalakote/Bagalkote, Belgaum/Belagavi,
-- Ballari/Bellary, Kalaburagi/Kalburgi, Mysore/Mysuru, Shimoga/Shivamogga,
-- Tumkur/Tumakuru, Yadagiri/Yadgir, Koppala, Chamarajanagara, Davangere) and
-- one row has a corrupted-encoding district name ("Bengaluru" + a stray
-- byte) - resolved to Bengaluru Rural by cross-referencing its taluk name
-- (Doddaballapura, a real Bengaluru Rural taluk, confirmed against this
-- same sheet's other Bengaluru Rural rows Devanahalli/Nelamangala). Every
-- one of the 110 rows below is pre-resolved to the exact config.district
-- spelling already live in this DB - additive, idempotent, safe to re-run.
IF NOT EXISTS (
	SELECT 1 FROM sys.tables WHERE schema_id = SCHEMA_ID('config') AND name = 'taluka'
)
BEGIN
	CREATE TABLE config.taluka (
		taluka_id INT IDENTITY(1,1) PRIMARY KEY,
		taluka_name NVARCHAR(200) NOT NULL,
		district_id INT NOT NULL,
		CONSTRAINT FK_taluka_district FOREIGN KEY (district_id) REFERENCES config.district(district_id)
	);
END
GO

INSERT INTO config.taluka (taluka_name, district_id)
SELECT v.taluka_name, d.district_id
FROM (VALUES
    (N'Bagalkot', N'Badami'),
    (N'Bagalkot', N'Hungund'),
    (N'Bagalkot', N'Jamakhandi'),
    (N'Belagavi', N'Athani'),
    (N'Belagavi', N'Chikkodi'),
    (N'Belagavi', N'Gokak'),
    (N'Belagavi', N'Raibhag'),
    (N'Belagavi', N'Saundatti'),
    (N'Ballari', N'Hadagali'),
    (N'Ballari', N'Harapanahalli'),
    (N'Ballari', N'Hospet'),
    (N'Bengaluru Rural', N'Doddaballapura'),
    (N'Bidar', N'Aurad'),
    (N'Bidar', N'Balki'),
    (N'Bidar', N'Basavakalyana'),
    (N'Bidar', N'Humnabad'),
    (N'Chamarajanagar', N'Gundlupet'),
    (N'Chamarajanagar', N'Kollegala'),
    (N'Chikkaballapur', N'Bagepalli'),
    (N'Chikkaballapur', N'Chintamani'),
    (N'Chikkaballapur', N'Gowribidanur'),
    (N'Chikkamagaluru', N'Kadur'),
    (N'Chikkamagaluru', N'Tarikere'),
    (N'Chitradurga', N'BelaguruCHC'),
    (N'Chitradurga', N'Challakere'),
    (N'Davanagere', N'Harappanahalli'),
    (N'Dharwad', N'Navalagunda'),
    (N'Gadag', N'Naragund'),
    (N'Gadag', N'Ron'),
    (N'Hassan', N'Arsikere'),
    (N'Hassan', N'Belur'),
    (N'Haveri', N'Byadagi'),
    (N'Haveri', N'Hirekeruru'),
    (N'Haveri', N'Ranebennur'),
    (N'Kalaburgi', N'Afzalpur'),
    (N'Kalaburgi', N'Aland'),
    (N'Kalaburgi', N'Chincholi'),
    (N'Kalaburgi', N'Chittapur'),
    (N'Kalaburgi', N'Jevargi'),
    (N'Kalaburgi', N'Sedam'),
    (N'Kodagu', N'Somwarpet'),
    (N'Kodagu', N'Virajpet'),
    (N'Kolar', N'Bangarpet'),
    (N'Kolar', N'Malur'),
    (N'Kolar', N'Mulbagal'),
    (N'Kolar', N'KGF'),
    (N'Koppal', N'Gangavathi'),
    (N'Koppal', N'Kustagi'),
    (N'Mandya', N'Malavalli'),
    (N'Mysuru', N'HDKote'),
    (N'Raichur', N'Lingsagur'),
    (N'Ramanagara', N'Kanakapura'),
    (N'Ramanagara', N'Soluru'),
    (N'Shivamogga', N'Bhadravathi'),
    (N'Shivamogga', N'Sagara'),
    (N'Shivamogga', N'Shikaripura'),
    (N'Shivamogga', N'Soraba'),
    (N'Shivamogga', N'Thirthahalli'),
    (N'Tumakuru', N'Koratagere'),
    (N'Tumakuru', N'Pavagada'),
    (N'Tumakuru', N'Sira'),
    (N'Tumakuru', N'Tiptur'),
    (N'Udupi', N'Karkala'),
    (N'Uttara Kannada', N'Ankola'),
    (N'Uttara Kannada', N'Bhatkal'),
    (N'Uttara Kannada', N'Haliyal'),
    (N'Uttara Kannada', N'Honnavar'),
    (N'Uttara Kannada', N'Joida'),
    (N'Uttara Kannada', N'Kumta'),
    (N'Uttara Kannada', N'Mundgod'),
    (N'Uttara Kannada', N'Siddapur'),
    (N'Uttara Kannada', N'Sirsi'),
    (N'Uttara Kannada', N'Yellapur'),
    (N'Vijayapura', N'Bagewadi'),
    (N'Vijayapura', N'Indi'),
    (N'Vijayapura', N'Sindagi'),
    (N'Yadgir', N'Shorapura'),
    (N'Bengaluru Urban', N'Bengaluru North - CV Raman Nagar'),
    (N'Bengaluru Urban', N'Bengaluru North - Pulikeshinagar'),
    (N'Bengaluru Urban', N'Anekal'),
    (N'Bengaluru Urban', N'Mahadevapura'),
    (N'Bengaluru Rural', N'Devanahalli'),
    (N'Bengaluru Rural', N'Nelamangala'),
    (N'Chitradurga', N'Holalkere'),
    (N'Chitradurga', N'Molakalmuru'),
    (N'Davanagere', N'Mayakonda'),
    (N'Davanagere', N'Jagaluru'),
    (N'Shivamogga', N'HolaluruCHC'),
    (N'Shivamogga', N'AyanuruCHC'),
    (N'Chikkamagaluru', N'Mudigere'),
    (N'Dakshina Kannada', N'Sullia'),
    (N'Hassan', N'Sakleshpur'),
    (N'Mysuru', N'T Narasipura'),
    (N'Mysuru', N'Nanjangudu'),
    (N'Belagavi', N'Kudachi'),
    (N'Belagavi', N'Yemkanmardi'),
    (N'Bagalkot', N'Mudhol'),
    (N'Vijayapura', N'Chadchan'),
    (N'Gadag', N'Shirahatti'),
    (N'Ballari', N'Hagaribommanahalli'),
    (N'Ballari', N'Siruguppa'),
    (N'Ballari', N'Kudligi'),
    (N'Ballari', N'Kampli'),
    (N'Ballari', N'Sanduru'),
    (N'Kalaburgi', N'Kalburgi Rural'),
    (N'Raichur', N'Matamari (PHC)'),
    (N'Raichur', N'Devadurga'),
    (N'Raichur', N'Manvi'),
    (N'Raichur', N'Maski'),
    (N'Koppal', N'Kanakagiri')
) AS v(district_name, taluka_name)
JOIN config.district d ON d.district_name = v.district_name
WHERE NOT EXISTS (
	SELECT 1 FROM config.taluka t WHERE t.district_id = d.district_id AND t.taluka_name = v.taluka_name
);
GO
