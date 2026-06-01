# PHASE1_BACKLOG - MediBook

## 1. Scope Phase 1 (đã chốt)
- `api-gateway`
- `identity-service`
- `patient-service`
- `doctor-schedule-service`
- `appointment-service`
- `payment-service`
- `notification-service`

Ngoài phạm vi Phase 1:
- `queue-service`
- `reporting-service`
- `audit-service`

## 2. Epic Breakdown

### EPIC-01: Identity & Access
#### US-01: Đăng ký/đăng nhập bệnh nhân
- Mô tả: Người dùng tạo tài khoản và đăng nhập để sử dụng hệ thống.
- Priority: P0
- Estimate: 5 SP
- Tasks:
1. Tạo API `POST /api/v1/auth/register`.
2. Tạo API `POST /api/v1/auth/login`.
3. Sinh access token + refresh token.
4. Mã hóa mật khẩu (BCrypt).
5. Thêm validate request và error mapping.
- Done Criteria:
1. Đăng ký và đăng nhập thành công qua Postman.
2. Token hợp lệ dùng được cho API có bảo vệ.

#### US-02: Phân quyền cơ bản
- Mô tả: Hỗ trợ role `PATIENT`, `RECEPTIONIST`, `ADMIN` cho Phase 1.
- Priority: P0
- Estimate: 3 SP
- Tasks:
1. Thiết kế bảng users/roles/user_roles.
2. Tạo RBAC filter/interceptor.
3. Chặn endpoint theo role.
- Done Criteria:
1. Endpoint admin bị từ chối với user thường (403).
2. Role map đúng theo token claims.

### EPIC-02: Patient Profile
#### US-03: Quản lý hồ sơ bệnh nhân
- Mô tả: Bệnh nhân tạo/cập nhật hồ sơ cá nhân.
- Priority: P0
- Estimate: 5 SP
- Tasks:
1. API tạo hồ sơ `POST /api/v1/patients`.
2. API xem/cập nhật hồ sơ `GET/PUT /api/v1/patients/{id}`.
3. Validate dữ liệu bắt buộc.
4. Mapping entity/dto/mapper đúng convention.
- Done Criteria:
1. CRUD hồ sơ hoạt động ổn định.
2. Không cho cập nhật hồ sơ người khác nếu không đủ quyền.

### EPIC-03: Doctor & Schedule
#### US-04: Quản lý bác sĩ và lịch trống
- Mô tả: Hiển thị bác sĩ/chuyên khoa và slot khám khả dụng.
- Priority: P0
- Estimate: 8 SP
- Tasks:
1. Thiết kế bảng doctors/specialties/schedules/slots.
2. API `GET /api/v1/doctors`.
3. API `GET /api/v1/schedules/slots` (filter theo ngày/cơ sở/chuyên khoa).
4. Index tối ưu truy vấn slot.
- Done Criteria:
1. Trả đúng danh sách slot available theo filter.
2. P95 endpoint đọc dưới ngưỡng mục tiêu.

### EPIC-04: Appointment Core
#### US-05: Đặt lịch khám
- Mô tả: Bệnh nhân đặt lịch trên slot trống.
- Priority: P0
- Estimate: 8 SP
- Tasks:
1. API `POST /api/v1/appointments`.
2. State ban đầu `PENDING_PAYMENT` hoặc `BOOKED` theo flow.
3. Chống double-booking (unique + lock).
4. Lưu `appointment_status_history`.
- Done Criteria:
1. 2 request đồng thời cùng slot chỉ 1 request thành công.
2. Trạng thái lịch lưu đúng lịch sử chuyển trạng thái.

#### US-06: Đổi/hủy lịch
- Mô tả: Bệnh nhân đổi/hủy theo policy thời gian.
- Priority: P0
- Estimate: 5 SP
- Tasks:
1. API `PATCH /api/v1/appointments/{id}/reschedule`.
2. API `PATCH /api/v1/appointments/{id}/cancel`.
3. Check policy deadline đổi/hủy.
4. Ghi reason và history.
- Done Criteria:
1. Đổi/hủy đúng policy.
2. Trạng thái cập nhật đúng và idempotent.

### EPIC-05: Payment Basic
#### US-07: Thanh toán online/offline cơ bản
- Mô tả: Ghi nhận giao dịch và callback cơ bản.
- Priority: P0
- Estimate: 8 SP
- Tasks:
1. API init payment `POST /api/v1/payments/init`.
2. API callback `POST /api/v1/payments/callback`.
3. Trạng thái payment: `INIT|SUCCESS|FAILED|REFUNDED`.
4. Liên kết payment với appointment.
- Done Criteria:
1. Callback thành công cập nhật đúng trạng thái.
2. Payment fail không làm sai trạng thái booking.

### EPIC-06: Notification Basic
#### US-08: Gửi xác nhận và nhắc lịch
- Mô tả: Gửi thông báo khi đặt lịch thành công và trước giờ khám.
- Priority: P1
- Estimate: 5 SP
- Tasks:
1. Thiết kế template thông báo.
2. Trigger gửi khi `BOOKED/CONFIRMED`.
3. Scheduler nhắc lịch T-24h/T-2h (cấu hình được).
4. Retry tối đa 3 lần khi gửi lỗi.
- Done Criteria:
1. Có log/tracking trạng thái gửi.
2. Tỉ lệ gửi thành công đạt target nội bộ.

### EPIC-07: API Gateway Integration
#### US-09: Gateway route cho toàn bộ service Phase 1
- Mô tả: Tất cả API Phase 1 truy cập qua gateway.
- Priority: P0
- Estimate: 3 SP
- Tasks:
1. Cấu hình route trong `api-gateway/application.yaml`.
2. Thêm filter auth cơ bản.
3. Chuẩn hóa prefix `/api/v1/*`.
- Done Criteria:
1. Gọi API service qua gateway hoạt động ổn định.
2. Không expose endpoint nội bộ không cần thiết.

### EPIC-08: Data, Migration, Quality
#### US-10: Migration và chuẩn DB cho từng service
- Mô tả: Mỗi service có migration độc lập, chạy được từ môi trường trống.
- Priority: P0
- Estimate: 8 SP
- Tasks:
1. Tạo `V1__init.sql` cho từng service Phase 1.
2. Cấu hình Flyway theo profile.
3. Validate schema + index + unique constraints.
- Done Criteria:
1. DB trống có thể migrate thành công 100%.
2. Không có mismatch entity/schema khi app startup.

#### US-11: Test tối thiểu cho Phase 1
- Mô tả: Bao phủ test cho luồng nghiệp vụ trọng yếu.
- Priority: P1
- Estimate: 8 SP
- Tasks:
1. Unit test service layer cho identity/appointment/payment.
2. Integration test luồng booking-payment-notification.
3. Contract test cho gateway route chính.
- Done Criteria:
1. Pipeline test pass.
2. Luồng E2E cơ bản pass theo kịch bản.

### EPIC-09: DevOps & Runtime
#### US-12: Chạy stack local/dev/prod bằng compose
- Mô tả: Chuẩn hóa vận hành theo file compose đã tách môi trường.
- Priority: P0
- Estimate: 5 SP
- Tasks:
1. Duy trì `docker-compose.yml` + `docker-compose.local.yml` + `docker-compose.dev.yml` + `docker-compose.prod.yml`.
2. Duy trì `.env.local-stack/.env.dev-stack/.env.prod-stack`.
3. Checklist runbook startup/shutdown.
- Done Criteria:
1. Local stack khởi động thành công.
2. Dev/prod profile đọc đúng env file.

## 3. Milestone Gợi ý (4 Sprint)
- Sprint 1: EPIC-01, EPIC-02, nền tảng gateway.
- Sprint 2: EPIC-03, EPIC-04 (đặt lịch).
- Sprint 3: EPIC-05, EPIC-06.
- Sprint 4: EPIC-08, EPIC-09, hardening + UAT.

## 4. Definition of Done (Phase 1)
1. Các API Phase 1 hoạt động qua gateway.
2. Luồng end-to-end: đăng nhập -> chọn slot -> đặt lịch -> thanh toán -> nhận thông báo.
3. Không còn lỗi P0/P1 mở.
4. Tài liệu config/runbook cập nhật đầy đủ trong README và docs.
