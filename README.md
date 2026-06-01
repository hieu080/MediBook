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
### 4.1 Spring profiles
Mỗi service có:
- `application.yaml` (base config, route, fixed port)
- `application-local.yaml`
- `application-dev.yaml`
- `application-prod.yaml`

### 4.2 Env files
Mỗi service có:
- `.env` (local)
- `.env.dev`
- `.env.prod`

Root project có file chọn env cho compose:
- `.env.local-stack`
- `.env.dev-stack`
- `.env.prod-stack`

## 5. Docker Compose Files
- `docker-compose.yml`: base services (app + redis + rabbitmq)
- `docker-compose.local.yml`: thêm PostgreSQL containers cho local
- `docker-compose.dev.yml`: dev override (dùng Postgre server ngoài)
- `docker-compose.prod.yml`: prod override (dùng Postgre server ngoài)

## 6. How To Configure DB
### 6.1 Local
Local dùng PostgreSQL trong Docker. Kiểm tra các file `service/.env` có `DB_HOST` phù hợp.

### 6.2 Dev/Prod
Dev/Prod dùng Postgre server ngoài Docker. Cập nhật trong mỗi `service/.env.dev` hoặc `service/.env.prod`:
- `DB_HOST`
- `DB_PORT`
- `DB_NAME`
- `DB_USERNAME`
- `DB_PASSWORD`
- `DB_SCHEMA`

## 7. Run With Docker Compose
### 7.1 Local
```bash
cd /home/hieu-pt/Documents/MediBook
docker compose -f docker-compose.yml -f docker-compose.local.yml --env-file .env.local-stack up --build
```

### 7.2 Dev
```bash
cd /home/hieu-pt/Documents/MediBook
docker compose -f docker-compose.yml -f docker-compose.dev.yml --env-file .env.dev-stack up --build -d
```

### 7.3 Prod
```bash
cd /home/hieu-pt/Documents/MediBook
docker compose -f docker-compose.yml -f docker-compose.prod.yml --env-file .env.prod-stack up --build -d
```

### 7.4 Stop
```bash
docker compose -f docker-compose.yml -f docker-compose.local.yml --env-file .env.local-stack down
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
6. Cách chạy local/dev/prod.
7. Cách build/test/deploy.
8. Danh sách service + port.
9. Troubleshooting thường gặp.
10. Tài liệu tham chiếu (BRD/SRS/SDD/DBD/API).

## 11. Troubleshooting
- `failed to connect to docker.sock`:
  - Docker daemon chưa chạy.
- Service không connect DB:
  - Kiểm tra `DB_HOST/DB_PORT/DB_NAME` trong env file tương ứng.
- Dev/Prod vẫn trỏ DB local:
  - Kiểm tra bạn đang dùng đúng `--env-file .env.dev-stack` hoặc `.env.prod-stack`.

## 12. Documents
- [BRD](/home/hieu-pt/Documents/MediBook/docs/BRD.md)
- [SRS](/home/hieu-pt/Documents/MediBook/docs/SRS.md)
- [SDD](/home/hieu-pt/Documents/MediBook/docs/SDD.md)
- [DBD](/home/hieu-pt/Documents/MediBook/docs/DBD.md)
- [Coding Convention](/home/hieu-pt/Documents/MediBook/docs/CODING_CONVENTION.md)
