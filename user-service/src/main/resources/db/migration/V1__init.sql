CREATE TABLE user_profiles (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    auth_user_id BIGINT NOT NULL,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    phone VARCHAR(64),
    created_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_user_profiles_auth_user_id ON user_profiles(auth_user_id);
