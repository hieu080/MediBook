# SDD - MediBook (Software Design Document)

## 1. Thông tin tài liệu
- Tên hệ thống: MediBook
- Loại tài liệu: Software Design Document (SDD)
- Phiên bản: v1.0
- Ngày cập nhật: 2026-06-01
- Tài liệu liên quan:
- [BRD.md](./BRD.md)
- [SRS.md](./SRS.md)

## 2. Mục tiêu thiết kế
- Chuyển yêu cầu SRS thành thiết kế kỹ thuật khả thi cho microservice.
- Chuẩn hóa kiến trúc, luồng giao tiếp, dữ liệu, bảo mật, logging, triển khai.
- Giảm rủi ro khi triển khai song song nhiều service.

## 3. Nguyên tắc thiết kế
- Domain-driven decomposition theo bounded context.
- Database per service, không truy cập chéo DB trực tiếp.
- API-first, contract rõ ràng, version hóa `/api/v1`.
- Ưu tiên eventual consistency qua event cho tác vụ không cần đồng bộ tức thời.
- Resilience: timeout, retry, circuit breaker, idempotency.

## 4. Kiến trúc tổng thể
### 4.1 Thành phần chính
- `api-gateway`: entrypoint cho web/mobile/portal, định tuyến và auth filter.
- `identity-service`: xác thực, token, RBAC.
- `patient-service`: hồ sơ bệnh nhân/người phụ thuộc.
- `doctor-schedule-service`: bác sĩ, chuyên khoa, lịch làm việc, slot.
- `appointment-service`: đặt/đổi/hủy lịch, state machine lịch hẹn.
- `payment-service`: giao dịch, callback cổng thanh toán, hoàn tiền.
- `queue-service`: check-in, cấp số thứ tự, trạng thái luồng khám.
- `notification-service`: template, gửi SMS/Email/Zalo, retry.
- `reporting-service`: tổng hợp KPI và dashboard.
- `audit-service` (khuyến nghị cho phase 1 nếu đủ nguồn lực): audit trail tập trung.

### 4.2 Thành phần hạ tầng
- PostgreSQL (mỗi service một schema hoặc một DB riêng).
- Redis: cache slot nóng, distributed lock, idempotency key.
- Message broker (RabbitMQ/Kafka): publish domain events.
- Object storage (tùy chọn giai đoạn sau): tài liệu y tế/đính kèm.

## 5. Kiến trúc runtime và giao tiếp
### 5.1 Giao tiếp đồng bộ
- Client -> Gateway -> Service qua HTTP/REST JSON.
- Service -> Service dùng REST nội bộ khi cần phản hồi ngay:
- `appointment-service` gọi `doctor-schedule-service` để reserve/release slot.
- `appointment-service` gọi `payment-service` để init payment.

### 5.2 Giao tiếp bất đồng bộ (event-driven)
- Event bus topics (đề xuất):
- `appointment.created`
- `appointment.rescheduled`
- `appointment.cancelled`
- `appointment.checked_in`
- `payment.succeeded`
- `payment.failed`
- `payment.refunded`
- `notification.requested`
- `audit.logged`
- `reporting.fact.updated`

### 5.3 Mẫu xử lý
- Saga orchestration nhẹ tại `appointment-service` cho flow đặt lịch có thanh toán.
- Outbox pattern cho event publishing đảm bảo không mất sự kiện.

## 6. Thiết kế theo service
### 6.1 Identity Service
- Trách nhiệm:
- Đăng ký/đăng nhập/refresh token.
- Quản lý user-role-permission.
- API chính:
- `POST /api/v1/auth/register`
- `POST /api/v1/auth/login`
- `POST /api/v1/auth/refresh`
- Dữ liệu:
- `users`, `roles`, `user_roles`, `sessions`, `login_history`.

### 6.2 Patient Service
- Trách nhiệm: CRUD hồ sơ bệnh nhân và người phụ thuộc.
- API chính:
- `GET /api/v1/patients/{id}`
- `POST /api/v1/patients`
- `PUT /api/v1/patients/{id}`
- Dữ liệu:
- `patients`, `patient_contacts`, `patient_dependents`.

### 6.3 Doctor-Schedule Service
- Trách nhiệm:
- Quản lý bác sĩ/chuyên khoa/cơ sở/phòng khám.
- Sinh và quản lý slot khám.
- API chính:
- `GET /api/v1/doctors`
- `GET /api/v1/schedules/slots`
- `POST /api/v1/slots/{slotId}/reserve`
- `POST /api/v1/slots/{slotId}/release`
- Dữ liệu:
- `doctors`, `specialties`, `facilities`, `schedules`, `slots`.
- Ràng buộc:
- Unique `(doctor_id, slot_start_time)`.

### 6.4 Appointment Service
- Trách nhiệm:
- Tạo/đổi/hủy lịch hẹn.
- Quản lý state machine lịch hẹn.
- API chính:
- `POST /api/v1/appointments`
- `PATCH /api/v1/appointments/{id}/reschedule`
- `PATCH /api/v1/appointments/{id}/cancel`
- `POST /api/v1/appointments/{id}/check-in`
- Dữ liệu:
- `appointments`, `appointment_status_history`, `appointment_notes`.
- Quy tắc kỹ thuật:
- Idempotency key cho create/cancel.
- Optimistic locking qua `version`.

### 6.5 Payment Service
- Trách nhiệm:
- Init giao dịch, verify callback, refund.
- API chính:
- `POST /api/v1/payments/init`
- `POST /api/v1/payments/callback`
- `POST /api/v1/payments/{id}/refund`
- Dữ liệu:
- `payments`, `payment_transactions`, `refunds`.
- Ghi chú:
- Callback phải verify chữ ký số từ gateway.

### 6.6 Queue Service
- Trách nhiệm:
- Check-in, cấp số thứ tự, gọi khám.
- API chính:
- `POST /api/v1/queue/check-in`
- `GET /api/v1/queue/tickets`
- `POST /api/v1/queue/tickets/{id}/call`
- Dữ liệu:
- `queue_tickets`, `queue_counters`, `queue_events`.

### 6.7 Notification Service
- Trách nhiệm:
- Render template, gửi đa kênh, retry.
- API/Event:
- Nhận `notification.requested` từ bus.
- Dữ liệu:
- `notification_templates`, `notification_messages`, `notification_attempts`.
- Retry policy:
- exponential backoff (1m, 5m, 15m), max 3 lần.

### 6.8 Reporting Service
- Trách nhiệm:
- Tập hợp số liệu KPI theo ngày/tuần/tháng.
- API chính:
- `GET /api/v1/reports/kpi`
- `GET /api/v1/reports/no-show`
- Dữ liệu:
- `fact_appointments`, `fact_payments`, `fact_queue`, `dim_time`, `dim_doctor`.

### 6.9 Audit Service (khuyến nghị)
- Trách nhiệm:
- Lưu log thay đổi đối tượng nghiệp vụ.
- Nhận `audit.logged` events từ các service khác.

## 7. Thiết kế dữ liệu liên service
### 7.1 Khóa định danh
- Sử dụng UUID cho `appointment_id`, `payment_id`, `ticket_id`.
- Sử dụng numeric/UUID cho `user_id`, `doctor_id`, `patient_id` tùy chiến lược thống nhất.

### 7.2 Chuẩn timestamp
- Lưu UTC trong DB.
- Convert timezone hiển thị ở client (mặc định `Asia/Ho_Chi_Minh`).

### 7.3 Data retention
- Audit log: tối thiểu 12 tháng.
- Payment transaction log: theo quy định tài chính nội bộ (khuyến nghị >= 24 tháng).

## 8. Luồng xử lý chi tiết
### 8.1 Đặt lịch có thanh toán online
1. Client gọi `POST /appointments` kèm idempotency key.
2. Appointment reserve slot ở Doctor-Schedule.
3. Appointment tạo bản ghi `PENDING_PAYMENT`.
4. Appointment gọi Payment `init`.
5. Gateway thanh toán callback về Payment.
6. Payment publish `payment.succeeded` hoặc `payment.failed`.
7. Appointment consume event:
- succeeded -> `BOOKED/CONFIRMED`, phát `appointment.created`.
- failed -> cancel tạm, release slot.
8. Notification consume event và gửi xác nhận.

### 8.2 Hủy lịch và hoàn tiền
1. Client gọi cancel.
2. Appointment kiểm tra policy.
3. Nếu đủ điều kiện hoàn tiền -> gọi Payment refund.
4. Cập nhật `CANCELLED`, release slot, phát event.
5. Notification gửi thông báo hủy.

## 9. Bảo mật thiết kế
- JWT access token + refresh token.
- Gateway thực hiện authN; service thực hiện authZ theo claim role/scope.
- Mã hóa PII (phone/email) ở lớp persistence hoặc KMS.
- Chống abuse:
- rate limit tại gateway.
- brute-force protection cho login.

## 10. Quan sát hệ thống và vận hành
- Logging:
- JSON structured logs, include `traceId`, `spanId`, `userId`, `appointmentId`.
- Metrics:
- latency, error rate, booking success ratio, payment failure ratio.
- Tracing:
- OpenTelemetry qua gateway và toàn bộ service.
- Alert:
- booking error > 3%/5 phút.
- payment callback timeout tăng bất thường.

## 11. Triển khai môi trường
### 11.1 Dev local
- Docker Compose chạy các service và hạ tầng.
- Mỗi service expose port riêng, gateway public port duy nhất.

### 11.2 Stage/Prod
- Khuyến nghị Kubernetes:
- HPA cho appointment, schedule, notification.
- ConfigMap/Secret tách biệt.
- Rolling update và readiness/liveness probes.

## 12. Cấu trúc repository đề xuất
```text
MediBook/
  gateway-service/
  identity-service/
  patient-service/
  doctor-schedule-service/
  appointment-service/
  payment-service/
  queue-service/
  notification-service/
  reporting-service/
  shared-kernel/
  docs/
    BRD.md
    SRS.md
    SDD.md
```

## 13. Quy ước code và API
- DTO tách biệt entity persistence.
- Mọi API thay đổi dữ liệu phải hỗ trợ idempotency khi cần.
- Chuẩn response:
- `code`, `message`, `data`, `traceId`.
- Chuẩn lỗi nghiệp vụ:
- `SLOT_NOT_AVAILABLE`, `BOOKING_DEADLINE_EXCEEDED`, `PAYMENT_FAILED`, `APPOINTMENT_NOT_FOUND`.

## 14. Mapping SRS -> Thiết kế
- SRS FR-01 -> Identity Service + JWT/RBAC gateway filter.
- SRS FR-03/FR-04 -> Doctor-Schedule + lock/reserve slot API.
- SRS FR-05/FR-06 -> Appointment state machine + policy engine.
- SRS FR-07 -> Payment callback verify + refund flow.
- SRS FR-08 -> Notification consumer + retry policy.
- SRS FR-09 -> Queue ticket lifecycle.
- SRS FR-10 -> Reporting fact tables + aggregation jobs.

## 15. Rủi ro kỹ thuật và giảm thiểu
- Race condition khi đặt slot đồng thời:
- Mitigation: DB unique + distributed lock + optimistic retry.
- Callback payment đến trễ/lặp:
- Mitigation: idempotent transaction state + signature verify.
- Mất event khi publish:
- Mitigation: outbox pattern + consumer retry + DLQ.
- Phụ thuộc provider notification:
- Mitigation: multi-provider abstraction + fallback channel.

## 16. Quyết định kỹ thuật cần chốt
- Chọn message broker: RabbitMQ hay Kafka.
- Chọn giải pháp auth: tự triển khai JWT hay Keycloak.
- Chọn chiến lược DB: 1 PostgreSQL nhiều schema hay nhiều instance.
- Chọn chuẩn API nội bộ: REST-only hay bổ sung gRPC.
- Mức ưu tiên triển khai `audit-service` ngay phase 1 hay phase 2.
