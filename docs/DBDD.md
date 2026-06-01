# DBDD - MediBook (Database Design Document)

## 1. Thông tin tài liệu
- Tên tài liệu: DBĐ (Database Design Document)
- Dự án: MediBook
- Phiên bản: v1.0
- Ngày cập nhật: 2026-06-01
- Tài liệu liên quan:
- [BRD.md](./BRD.md)
- [SRS.md](./SRS.md)
- [SDD.md](./SDD.md)

## 2. Mục tiêu
- Thiết kế dữ liệu cho hệ thống microservice MediBook.
- Chuẩn hóa bảng, khóa, ràng buộc, chỉ mục để hỗ trợ nghiệp vụ đặt lịch khám.
- Làm baseline cho migration script và tối ưu truy vấn.

## 3. Nguyên tắc thiết kế dữ liệu
- Database per service (tách schema theo bounded context).
- Dùng `UUID` cho khóa nghiệp vụ chính (appointment/payment/ticket).
- Lưu thời gian dạng `TIMESTAMPTZ` theo UTC.
- Soft delete khi cần audit (`deleted_at`), tránh xóa cứng dữ liệu nghiệp vụ.
- Bắt buộc `created_at`, `updated_at`, `created_by`, `updated_by`.

## 4. Phân tách schema
- `identity_db`
- `patient_db`
- `doctor_schedule_db`
- `appointment_db`
- `payment_db`
- `queue_db`
- `notification_db`
- `reporting_db`
- `audit_db` (khuyến nghị)

## 5. Thiết kế bảng theo service
### 5.1 Identity DB
#### `users`
- `user_id UUID PK`
- `username VARCHAR(100) UNIQUE NOT NULL`
- `password_hash VARCHAR(255) NOT NULL`
- `email VARCHAR(150) UNIQUE`
- `phone VARCHAR(20) UNIQUE`
- `status VARCHAR(20) NOT NULL` (`ACTIVE|LOCKED|INACTIVE`)
- `last_login_at TIMESTAMPTZ`
- `created_at TIMESTAMPTZ NOT NULL`
- `updated_at TIMESTAMPTZ NOT NULL`

#### `roles`
- `role_id UUID PK`
- `role_code VARCHAR(50) UNIQUE NOT NULL`
- `role_name VARCHAR(100) NOT NULL`

#### `user_roles`
- `user_id UUID NOT NULL`
- `role_id UUID NOT NULL`
- `PRIMARY KEY(user_id, role_id)`

#### `sessions`
- `session_id UUID PK`
- `user_id UUID NOT NULL`
- `refresh_token_hash VARCHAR(255) NOT NULL`
- `expires_at TIMESTAMPTZ NOT NULL`
- `revoked_at TIMESTAMPTZ`

---
### 5.2 Patient DB
#### `patients`
- `patient_id UUID PK`
- `user_id UUID UNIQUE`
- `full_name VARCHAR(150) NOT NULL`
- `dob DATE NOT NULL`
- `gender VARCHAR(10)`
- `phone VARCHAR(20) NOT NULL`
- `email VARCHAR(150)`
- `address TEXT`
- `emergency_contact_name VARCHAR(150)`
- `emergency_contact_phone VARCHAR(20)`
- `created_at TIMESTAMPTZ NOT NULL`
- `updated_at TIMESTAMPTZ NOT NULL`

#### `patient_dependents`
- `dependent_id UUID PK`
- `patient_id UUID NOT NULL`
- `full_name VARCHAR(150) NOT NULL`
- `dob DATE`
- `relationship VARCHAR(50) NOT NULL`
- `phone VARCHAR(20)`
- `created_at TIMESTAMPTZ NOT NULL`
- `updated_at TIMESTAMPTZ NOT NULL`

---
### 5.3 Doctor Schedule DB
#### `facilities`
- `facility_id UUID PK`
- `facility_name VARCHAR(150) NOT NULL`
- `address TEXT NOT NULL`
- `status VARCHAR(20) NOT NULL`

#### `specialties`
- `specialty_id UUID PK`
- `specialty_code VARCHAR(50) UNIQUE NOT NULL`
- `specialty_name VARCHAR(150) NOT NULL`

#### `doctors`
- `doctor_id UUID PK`
- `full_name VARCHAR(150) NOT NULL`
- `specialty_id UUID NOT NULL`
- `facility_id UUID NOT NULL`
- `status VARCHAR(20) NOT NULL`

#### `schedules`
- `schedule_id UUID PK`
- `doctor_id UUID NOT NULL`
- `work_date DATE NOT NULL`
- `start_time TIME NOT NULL`
- `end_time TIME NOT NULL`
- `slot_duration_min INT NOT NULL`
- `status VARCHAR(20) NOT NULL`

#### `slots`
- `slot_id UUID PK`
- `schedule_id UUID NOT NULL`
- `doctor_id UUID NOT NULL`
- `facility_id UUID NOT NULL`
- `slot_start TIMESTAMPTZ NOT NULL`
- `slot_end TIMESTAMPTZ NOT NULL`
- `status VARCHAR(20) NOT NULL` (`AVAILABLE|HELD|BOOKED|CLOSED`)
- `hold_expires_at TIMESTAMPTZ`
- `version INT NOT NULL DEFAULT 0`

Ràng buộc chính:
- `UNIQUE(doctor_id, slot_start)`
- `CHECK(slot_end > slot_start)`

---
### 5.4 Appointment DB
#### `appointments`
- `appointment_id UUID PK`
- `patient_id UUID NOT NULL`
- `doctor_id UUID NOT NULL`
- `facility_id UUID NOT NULL`
- `slot_id UUID NOT NULL`
- `status VARCHAR(30) NOT NULL`
- `reason TEXT`
- `channel VARCHAR(20) NOT NULL` (`APP|WEB|COUNTER`)
- `booking_code VARCHAR(20) UNIQUE NOT NULL`
- `no_show_marked BOOLEAN NOT NULL DEFAULT FALSE`
- `created_at TIMESTAMPTZ NOT NULL`
- `updated_at TIMESTAMPTZ NOT NULL`
- `version INT NOT NULL DEFAULT 0`

#### `appointment_status_history`
- `history_id UUID PK`
- `appointment_id UUID NOT NULL`
- `from_status VARCHAR(30)`
- `to_status VARCHAR(30) NOT NULL`
- `changed_by UUID`
- `changed_reason TEXT`
- `changed_at TIMESTAMPTZ NOT NULL`

#### `appointment_policies_snapshot`
- `snapshot_id UUID PK`
- `appointment_id UUID UNIQUE NOT NULL`
- `reschedule_deadline_min INT NOT NULL`
- `cancel_deadline_min INT NOT NULL`
- `refund_rule_json JSONB NOT NULL`
- `created_at TIMESTAMPTZ NOT NULL`

Ràng buộc chính:
- `UNIQUE(slot_id)` cho appointment active dùng partial index:
- `CREATE UNIQUE INDEX uq_appointment_active_slot ON appointments(slot_id) WHERE status IN ('BOOKED','CONFIRMED','CHECKED_IN','IN_PROGRESS');`

---
### 5.5 Payment DB
#### `payments`
- `payment_id UUID PK`
- `appointment_id UUID NOT NULL`
- `amount NUMERIC(14,2) NOT NULL`
- `currency VARCHAR(10) NOT NULL DEFAULT 'VND'`
- `method VARCHAR(30) NOT NULL`
- `status VARCHAR(20) NOT NULL` (`INIT|SUCCESS|FAILED|REFUNDED`)
- `provider VARCHAR(30) NOT NULL`
- `provider_txn_ref VARCHAR(100)`
- `created_at TIMESTAMPTZ NOT NULL`
- `updated_at TIMESTAMPTZ NOT NULL`

#### `payment_transactions`
- `txn_id UUID PK`
- `payment_id UUID NOT NULL`
- `event_type VARCHAR(30) NOT NULL` (`INIT|CALLBACK|REFUND`)
- `payload JSONB`
- `signature_valid BOOLEAN`
- `processed_at TIMESTAMPTZ NOT NULL`

#### `refunds`
- `refund_id UUID PK`
- `payment_id UUID NOT NULL`
- `refund_amount NUMERIC(14,2) NOT NULL`
- `reason TEXT`
- `status VARCHAR(20) NOT NULL`
- `provider_refund_ref VARCHAR(100)`
- `created_at TIMESTAMPTZ NOT NULL`

---
### 5.6 Queue DB
#### `queue_tickets`
- `ticket_id UUID PK`
- `appointment_id UUID UNIQUE NOT NULL`
- `facility_id UUID NOT NULL`
- `doctor_id UUID NOT NULL`
- `queue_no INT NOT NULL`
- `priority_level INT NOT NULL DEFAULT 0`
- `status VARCHAR(20) NOT NULL` (`WAITING|CALLED|SKIPPED|DONE`)
- `checked_in_at TIMESTAMPTZ NOT NULL`
- `called_at TIMESTAMPTZ`
- `completed_at TIMESTAMPTZ`

#### `queue_counters`
- `counter_id UUID PK`
- `facility_id UUID NOT NULL`
- `counter_code VARCHAR(20) NOT NULL`
- `status VARCHAR(20) NOT NULL`

---
### 5.7 Notification DB
#### `notification_templates`
- `template_id UUID PK`
- `template_code VARCHAR(50) UNIQUE NOT NULL`
- `channel VARCHAR(20) NOT NULL`
- `title VARCHAR(200)`
- `body TEXT NOT NULL`
- `active BOOLEAN NOT NULL DEFAULT TRUE`

#### `notification_messages`
- `message_id UUID PK`
- `appointment_id UUID`
- `patient_id UUID`
- `channel VARCHAR(20) NOT NULL`
- `target VARCHAR(150) NOT NULL`
- `template_code VARCHAR(50) NOT NULL`
- `status VARCHAR(20) NOT NULL` (`PENDING|SENT|FAILED`)
- `scheduled_at TIMESTAMPTZ`
- `sent_at TIMESTAMPTZ`

#### `notification_attempts`
- `attempt_id UUID PK`
- `message_id UUID NOT NULL`
- `attempt_no INT NOT NULL`
- `provider_response TEXT`
- `status VARCHAR(20) NOT NULL`
- `attempted_at TIMESTAMPTZ NOT NULL`

---
### 5.8 Reporting DB
#### `fact_appointments_daily`
- `fact_date DATE NOT NULL`
- `facility_id UUID NOT NULL`
- `doctor_id UUID NOT NULL`
- `specialty_id UUID NOT NULL`
- `total_booked INT NOT NULL`
- `total_cancelled INT NOT NULL`
- `total_no_show INT NOT NULL`
- `avg_waiting_min NUMERIC(8,2) NOT NULL`
- `PRIMARY KEY(fact_date, facility_id, doctor_id, specialty_id)`

#### `fact_payments_daily`
- `fact_date DATE NOT NULL`
- `facility_id UUID NOT NULL`
- `total_success_amount NUMERIC(14,2) NOT NULL`
- `total_refund_amount NUMERIC(14,2) NOT NULL`
- `success_count INT NOT NULL`
- `failed_count INT NOT NULL`
- `PRIMARY KEY(fact_date, facility_id)`

## 6. Quan hệ logic liên service
- `appointments.patient_id` tham chiếu logic đến `patients.patient_id` (không FK chéo DB).
- `appointments.slot_id` tham chiếu logic đến `slots.slot_id`.
- `payments.appointment_id` tham chiếu logic đến `appointments.appointment_id`.
- `queue_tickets.appointment_id` tham chiếu logic đến `appointments.appointment_id`.

Kiểm soát nhất quán:
- Xác thực tồn tại qua API call hoặc event projection.
- Dùng outbox + retry để đồng bộ eventual consistency.

## 7. Chỉ mục đề xuất
- `slots(doctor_id, slot_start, status)`
- `slots(facility_id, slot_start, status)`
- `appointments(patient_id, created_at DESC)`
- `appointments(doctor_id, status, created_at DESC)`
- `payments(appointment_id)`
- `payments(status, created_at DESC)`
- `queue_tickets(facility_id, status, checked_in_at)`
- `notification_messages(status, scheduled_at)`

## 8. Partitioning và lưu trữ
- Khuyến nghị partition theo tháng cho:
- `appointment_status_history`
- `payment_transactions`
- `notification_attempts`
- `audit_logs` (nếu có)

## 9. Migration strategy
- Dùng Flyway hoặc Liquibase cho từng service.
- Version migration độc lập theo service, ví dụ:
- `V1__init_identity.sql`
- `V1__init_patient.sql`
- `V2__add_partial_index_appointment.sql`
- Không sửa migration đã chạy production; chỉ thêm migration mới.

## 10. Backup và DR
- Full backup hằng ngày, WAL/incremental mỗi 15 phút.
- RPO mục tiêu: <= 15 phút.
- RTO mục tiêu: <= 2 giờ cho dịch vụ lõi đặt lịch.

## 11. Bảo mật dữ liệu
- Mã hóa kết nối DB bằng TLS.
- Hạn chế quyền DB user theo service (least privilege).
- Ẩn/mask dữ liệu nhạy cảm trong log (`phone`, `email`).

## 12. DDL mẫu (PostgreSQL)
```sql
CREATE TABLE appointments (
  appointment_id UUID PRIMARY KEY,
  patient_id UUID NOT NULL,
  doctor_id UUID NOT NULL,
  facility_id UUID NOT NULL,
  slot_id UUID NOT NULL,
  status VARCHAR(30) NOT NULL,
  reason TEXT,
  channel VARCHAR(20) NOT NULL,
  booking_code VARCHAR(20) UNIQUE NOT NULL,
  no_show_marked BOOLEAN NOT NULL DEFAULT FALSE,
  created_at TIMESTAMPTZ NOT NULL,
  updated_at TIMESTAMPTZ NOT NULL,
  version INT NOT NULL DEFAULT 0
);

CREATE UNIQUE INDEX uq_appointment_active_slot
ON appointments(slot_id)
WHERE status IN ('BOOKED','CONFIRMED','CHECKED_IN','IN_PROGRESS');
```

## 13. Quyết định cần chốt
- Dùng 1 PostgreSQL nhiều schema hay nhiều database instance.
- Chuẩn UUID generation (`gen_random_uuid()` hay từ app layer).
- Chính sách retention chi tiết cho bảng lịch sử/audit.
- Có triển khai `audit_db` từ phase 1 hay phase 2.
