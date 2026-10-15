-- Additive only: inserts one new "Unit D" row into config.unit for every
-- hospital that doesn't already have one (151 of 153 hospitals currently
-- have only Unit A/B/C; 2 pilot hospitals already have Unit D or beyond -
-- those are skipped by the NOT EXISTS guard, this script is idempotent).
-- No existing config.unit row is touched, updated, or removed.
INSERT INTO config.unit (unit, description, h_id, created_on, last_updated_on)
SELECT 'Unit D', 'Unit D', h.h_id, GETDATE(), GETDATE()
FROM (SELECT DISTINCT h_id FROM config.unit) h
WHERE NOT EXISTS (
    SELECT 1 FROM config.unit u2 WHERE u2.h_id = h.h_id AND u2.unit = 'Unit D'
);
