-- One-time fix: nr.rcho row 3 (Dr.Abhinav, Chitradurga) was created through
-- the standalone Add RCHO form before the district-anchored-credentials fix
-- (see RchoService#createRcho) - its login got generated from the person's
-- name ("rchodrabhinav") instead of the district. Resets the login to the
-- district-based scheme every other RCHO/hospital account already follows;
-- rcho_name ("Dr.Abhinav", the display label) is untouched.
UPDATE nr.nrc_users
SET user_id = 'rchochitradurga',
    password = '$2b$12$t6SkpkKCzgUi4ZFe2KF1Ie3CResUhjF8vdhcExGoFflJAND/RuZAu',
    must_change_password = 1,
    last_updated_on = GETDATE()
WHERE u_id = 2206;
