INSERT INTO roles (code, name)
VALUES ('DOCTOR', 'Bac si')
    ON CONFLICT (code) DO NOTHING;