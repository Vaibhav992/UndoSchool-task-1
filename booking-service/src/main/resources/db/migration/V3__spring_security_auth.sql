ALTER TABLE user_profiles ADD COLUMN IF NOT EXISTS email VARCHAR(255);
ALTER TABLE user_profiles ADD COLUMN IF NOT EXISTS password_hash VARCHAR(255);

UPDATE user_profiles SET email = auth_user_id WHERE email IS NULL AND auth_user_id IS NOT NULL;

ALTER TABLE user_profiles DROP COLUMN IF EXISTS auth_user_id;

ALTER TABLE user_profiles ALTER COLUMN email SET NOT NULL;
ALTER TABLE user_profiles ALTER COLUMN password_hash SET NOT NULL;

CREATE UNIQUE INDEX IF NOT EXISTS uq_user_profiles_email ON user_profiles(email);
