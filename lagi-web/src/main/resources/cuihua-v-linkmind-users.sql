-- Run this against the Cuihua SQLite database before starting this LinkMind branch.
-- The same DROP/CREATE pair should later be owned by a Cuihua Alembic migration.
BEGIN IMMEDIATE;

DROP VIEW IF EXISTS v_linkmind_users;

CREATE VIEW v_linkmind_users AS
SELECT
    u.id AS user_id,
    u.username AS username,
    u.email AS email,
    u.display_name AS display_name,
    u.password_hash AS password_hash,
    CASE
        WHEN EXISTS (
            SELECT 1
            FROM user_roles ur
            JOIN roles r ON r.id = ur.role_id
            WHERE ur.user_id = u.id
              AND r.code = 'system_admin'
        ) THEN 'system_admin'
        ELSE 'user'
    END AS role,
    u.status AS status,
    u.locked_until AS locked_until,
    CAST(strftime('%s', u.created_at) AS INTEGER) * 1000 AS created_at
FROM users u
WHERE LOWER(u.status) = 'active';

COMMIT;
