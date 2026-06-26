ALTER TABLE users
    ALTER COLUMN password_hash DROP NOT NULL;

CREATE TABLE external_identities (
    id BIGSERIAL PRIMARY KEY,
    provider VARCHAR(50) NOT NULL,
    external_subject VARCHAR(150) NOT NULL,
    user_id BIGINT NOT NULL,
    username VARCHAR(150),
    email VARCHAR(150),
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    CONSTRAINT fk_external_identities_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT uk_external_identities_provider_subject UNIQUE (provider, external_subject),
    CONSTRAINT uk_external_identities_provider_user UNIQUE (provider, user_id)
);

CREATE INDEX idx_external_identities_user_id ON external_identities (user_id);
CREATE INDEX idx_external_identities_provider ON external_identities (provider);

DROP TABLE IF EXISTS refresh_tokens;
