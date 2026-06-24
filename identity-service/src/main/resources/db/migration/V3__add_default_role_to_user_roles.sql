ALTER TABLE user_roles
ADD COLUMN is_default BOOLEAN NOT NULL DEFAULT FALSE;

UPDATE user_roles ur
SET is_default = TRUE
FROM roles r
WHERE ur.role_id = r.id
  AND r.code = 'PATIENT'
  AND ur.deleted_at IS NULL;

UPDATE user_roles ur
SET is_default = TRUE
WHERE ur.deleted_at IS NULL
  AND NOT EXISTS (
      SELECT 1
      FROM user_roles existing_default
      WHERE existing_default.user_id = ur.user_id
        AND existing_default.is_default = TRUE
        AND existing_default.deleted_at IS NULL
  )
  AND ur.id = (
      SELECT MIN(first_role.id)
      FROM user_roles first_role
      WHERE first_role.user_id = ur.user_id
        AND first_role.deleted_at IS NULL
  );

CREATE UNIQUE INDEX uk_user_roles_one_default_per_user
ON user_roles (user_id)
WHERE is_default = TRUE AND deleted_at IS NULL;
