# MediBook

Hospital Appointment System theo kiến trúc microservice.

## 1. Project Structure
- `api-gateway`
- `identity-service`
- `patient-service`
- `doctor-schedule-service`
- `appointment-service`
- `payment-service`
- `queue-service`
- `notification-service`
- `reporting-service`
- `audit-service`
- `docs/` (BRD, SRS, SDD, DBD, Coding Convention)


## Phase 1 Scope (Business-aligned)
Triển khai chính thức Phase 1 tập trung các service:
- `api-gateway`
- `identity-service`
- `patient-service`
- `doctor-schedule-service`
- `appointment-service`
- `payment-service`
- `notification-service`

Các service sau được scaffold sẵn nhưng ưu tiên triển khai ở Phase 2:
- `queue-service`
- `reporting-service`
- `audit-service`

## 2. Tech Stack
- Java 17
- Spring Boot 4
- Spring Cloud Gateway
- PostgreSQL
- Flyway
- Docker / Docker Compose
- RabbitMQ, Redis

## 3. Prerequisites
- JDK 17+
- Maven 3.9+
- Docker + Docker Compose

## 4. Configuration Model
### 4.1 Spring profile
Mỗi service chỉ giữ cấu hình local:
- `application.yaml` (base config, route, fixed port, active profile mặc định)
- `application-local.yaml` (default chạy IDE/local)

`application.yaml` đang active profile mặc định là `local`:

```yaml
spring:
  profiles:
    active: local
```

Vì vậy khi chạy trên IDE không cần set profile.

### 4.2 Local configuration
`application-local.yaml` của từng service đã có sẵn default DB để chạy từ IDE:
- host: `localhost`
- user/password: `postgres` / `postgres`
- schema: `public`
- DB name và port đúng với compose local

Docker không dùng các file `.env` service cho local nữa. Các biến cần cho container network được khai báo trực tiếp trong `docker-compose.yml`.

Identity local tự tạo `identity-service/keys/jwt-private.pem` và `identity-service/keys/jwt-public.pem` nếu chưa có. Trong service, path mặc định là `keys/...`; Docker override sang `/app/identity-service/keys/...` và bind mount về thư mục này. Hai file `.pem` được ignore khỏi git để tránh commit secret.

## 5. Docker Compose Files
- `docker-compose.yml`: local stack tự đủ app + PostgreSQL + Redis + RabbitMQ

## 6. How To Configure DB
### 6.1 Local IDE
Chạy database bằng Docker, sau đó run service trực tiếp trong IDE. Không cần set env nếu dùng cấu hình local mặc định.

```bash
cd /home/hieu-pt/Documents/MediBook
docker compose up -d postgres-identity postgres-patient postgres-doctor-schedule postgres-appointment postgres-payment postgres-queue postgres-notification postgres-reporting postgres-audit redis rabbitmq
```

JDBC URLs local:
- Identity: `jdbc:postgresql://localhost:5433/medibook_identity_db`
- Patient: `jdbc:postgresql://localhost:5434/medibook_patient_db`
- Doctor Schedule: `jdbc:postgresql://localhost:5435/medibook_doctor_schedule_db`
- Appointment: `jdbc:postgresql://localhost:5436/medibook_appointment_db`
- Payment: `jdbc:postgresql://localhost:5437/medibook_payment_db`
- Queue: `jdbc:postgresql://localhost:5438/medibook_queue_db`
- Notification: `jdbc:postgresql://localhost:5439/medibook_notification_db`
- Reporting: `jdbc:postgresql://localhost:5440/medibook_reporting_db`
- Audit: `jdbc:postgresql://localhost:5441/medibook_audit_db`

Thông tin xác thực mặc định:
- user: `postgres`
- password: `postgres`

### 6.2 Docker local stack
Compose tự set `DB_HOST` theo tên container PostgreSQL và route host service cho API Gateway.

```bash
cd /home/hieu-pt/Documents/MediBook
docker compose up --build -d
```

## 7. Run With Docker Compose
### 7.1 Local full stack
```bash
cd /home/hieu-pt/Documents/MediBook
docker compose up --build -d
```

### 7.2 Local infrastructure only for IDE
```bash
cd /home/hieu-pt/Documents/MediBook
docker compose up -d postgres-identity postgres-patient postgres-doctor-schedule postgres-appointment postgres-payment postgres-queue postgres-notification postgres-reporting postgres-audit redis rabbitmq
```

### 7.3 Stop
```bash
docker compose down
```

## 8. Build Source (without Docker)
```bash
cd /home/hieu-pt/Documents/MediBook
mvn -q -DskipTests package
```

## 9. Default Ports
- Gateway: `8080`
- Identity: `8081`
- Patient: `8082`
- Doctor Schedule: `8083`
- Appointment: `8084`
- Payment: `8085`
- Queue: `8086`
- Notification: `8087`
- Reporting: `8088`
- Audit: `8089`

Local PostgreSQL containers (host ports): `5433`..`5441`.

## 10. Recommended README Checklist (for any complete project)
Một README đầy đủ nên có:
1. Mục tiêu dự án và phạm vi.
2. Kiến trúc tổng quan.
3. Công nghệ sử dụng.
4. Yêu cầu môi trường chạy.
5. Cấu hình env và secret.
6. Cách chạy local.
7. Cách build/test/deploy.
8. Danh sách service + port.
9. Troubleshooting thường gặp.
10. Tài liệu tham chiếu (BRD/SRS/SDD/DBD/API).

## 11. Troubleshooting
- `failed to connect to docker.sock`:
  - Docker daemon chưa chạy.
- Service không connect DB:
  - Với IDE: kiểm tra service đang dùng profile `local` và DB container đã chạy.
  - Với Docker: kiểm tra `docker compose config` có đúng `DB_HOST/DB_PORT/DB_NAME`.

## 12. Documents
- [BRD](/home/hieu-pt/Documents/MediBook/docs/BRD.md)
- [SRS](/home/hieu-pt/Documents/MediBook/docs/SRS.md)
- [SDD](/home/hieu-pt/Documents/MediBook/docs/SDD.md)
- [DBD](/home/hieu-pt/Documents/MediBook/docs/DBD.md)
- [Coding Convention](/home/hieu-pt/Documents/MediBook/docs/CODING_CONVENTION.md)
