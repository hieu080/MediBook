CREATE TABLE patients (
    id BIGSERIAL PRIMARY KEY,
    public_id UUID NOT NULL,
    user_id UUID,
    full_name VARCHAR(150) NOT NULL,
    date_of_birth DATE NOT NULL,
    gender VARCHAR(20) NOT NULL,
    phone_number VARCHAR(20),
    email VARCHAR(100),
    address VARCHAR(255),
    insurance_number VARCHAR(50),
    emergency_contact_name VARCHAR(150),
    emergency_contact_phone VARCHAR(20),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP,
    CONSTRAINT uk_patients_public_id UNIQUE (public_id),
    CONSTRAINT ck_patients_gender CHECK (gender IN ('MALE', 'FEMALE', 'OTHER', 'UNKNOWN'))
);

CREATE UNIQUE INDEX uk_patients_user_id_not_null
    ON patients (user_id)
    WHERE user_id IS NOT NULL;

CREATE UNIQUE INDEX uk_patients_email_not_null
    ON patients (email)
    WHERE email IS NOT NULL;

CREATE UNIQUE INDEX uk_patients_insurance_number_not_null
    ON patients (insurance_number)
    WHERE insurance_number IS NOT NULL;

CREATE INDEX idx_patients_full_name
    ON patients (full_name);

CREATE INDEX idx_patients_phone_number
    ON patients (phone_number);

CREATE TABLE patient_relationships (
    id BIGSERIAL PRIMARY KEY,
    public_id UUID NOT NULL,
    patient_public_id UUID NOT NULL,
    related_user_public_id UUID NOT NULL,
    relationship_type VARCHAR(50) NOT NULL,
    permission_level VARCHAR(30) NOT NULL,
    is_primary_contact BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP,
    CONSTRAINT uk_patient_relationships_public_id UNIQUE (public_id),
    CONSTRAINT fk_patient_relationships_patient_public_id
        FOREIGN KEY (patient_public_id) REFERENCES patients (public_id),
    CONSTRAINT ck_patient_relationships_type CHECK (
        relationship_type IN ('PARENT', 'CHILD', 'SPOUSE', 'GUARDIAN', 'CAREGIVER', 'SELF', 'OTHER')
    ),
    CONSTRAINT ck_patient_relationships_permission CHECK (
        permission_level IN ('OWNER', 'MANAGER', 'VIEWER')
    )
);

CREATE UNIQUE INDEX uk_patient_relationships_active_relation
    ON patient_relationships (patient_public_id, related_user_public_id, relationship_type)
    WHERE deleted_at IS NULL;

CREATE UNIQUE INDEX uk_patient_relationships_primary_contact
    ON patient_relationships (patient_public_id)
    WHERE is_primary_contact = TRUE AND deleted_at IS NULL;

CREATE INDEX idx_patient_relationships_related_user_public_id_deleted_at
    ON patient_relationships (related_user_public_id, deleted_at);

CREATE INDEX idx_patient_relationships_patient_public_id_deleted_at
    ON patient_relationships (patient_public_id, deleted_at);
