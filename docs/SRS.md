# SRS - MediBook (Software Requirements Specification)

## 1. Thông tin tài liệu
- Tên hệ thống: MediBook
- Loại tài liệu: Software Requirements Specification (SRS)
- Phiên bản: v1.0
- Ngày cập nhật: 2026-06-01
- Tài liệu nguồn: [BRD.md](./BRD.md)

## 2. Mục tiêu tài liệu
- Chuyển hóa yêu cầu nghiệp vụ trong BRD thành yêu cầu phần mềm có thể phát triển và kiểm thử.
- Làm baseline cho team Dev, QA, BA, DevOps.

## 3. Phạm vi hệ thống (MVP)
- Quản lý tài khoản và phân quyền.
- Quản lý hồ sơ bệnh nhân.
- Quản lý lịch bác sĩ/slot khám.
- Đặt lịch, đổi lịch, hủy lịch.
- Thanh toán online/offline.
- Gửi thông báo nhắc hẹn/cập nhật lịch.


### 3.1 Ngoài phạm vi Phase 1 (đưa sang Phase 2)
- Check-in và quản lý hàng đợi (`queue-service`).
- Báo cáo KPI/dashboards (`reporting-service`).
- Audit service tập trung.

## 4. Thuật ngữ và viết tắt
- BRD: Business Requirements Document
- SRS: Software Requirements Specification
- RBAC: Role-Based Access Control
- SLA: Service Level Agreement
- MVP: Minimum Viable Product
- HIS/EMR: Hospital Information System / Electronic Medical Record

## 5. Tổng quan kiến trúc logic
- API Gateway/BFF nhận request từ web/mobile/portal.
- Các microservice domain:
- Identity Service
- Patient Profile Service
- Doctor & Schedule Service
- Appointment Service
- Queue Service
- Billing/Payment Service
- Notification Service
- Reporting Service
- Trao đổi dữ liệu:
- Đồng bộ: REST/gRPC nội bộ.
- Bất đồng bộ: Event bus cho notification, audit, reporting.

## 6. Actor hệ thống
- Patient
- Doctor
- Receptionist
- Admin
- Billing Staff
- External System (Payment Gateway, SMS/Email/Zalo Provider)

## 7. Use Case Specification
### UC-01: Đăng ký/Đăng nhập
- Actor: Patient, Staff
- Tiền điều kiện: chưa đăng nhập.
- Luồng chính:
1. Người dùng nhập thông tin xác thực.
2. Identity Service xác thực và phát hành access token.
3. Gateway trả token và profile role.
- Hậu điều kiện: phiên đăng nhập hợp lệ.
- Ngoại lệ:
- Sai mật khẩu quá ngưỡng -> khóa tạm thời.
- Token hết hạn -> bắt buộc refresh/đăng nhập lại.

### UC-02: Tìm kiếm lịch khám
- Actor: Patient, Receptionist
- Tiền điều kiện: có dữ liệu bác sĩ/chuyên khoa.
- Luồng chính:
1. Người dùng chọn cơ sở/chuyên khoa/bác sĩ/ngày.
2. Schedule Service trả danh sách slot khả dụng.
3. UI hiển thị slot theo thời gian thực.
- Hậu điều kiện: người dùng chọn được slot hợp lệ.

### UC-03: Đặt lịch khám
- Actor: Patient, Receptionist
- Tiền điều kiện: slot còn trống.
- Luồng chính:
1. Appointment Service giữ slot tạm thời (`PENDING_PAYMENT`).
2. Nếu online payment: chuyển Billing Service xử lý.
3. Thành công -> `BOOKED`/`CONFIRMED`.
4. Notification Service gửi xác nhận.
- Hậu điều kiện: tạo lịch hẹn thành công với `appointment_id`.
- Ngoại lệ:
- Slot bị chiếm trong lúc thanh toán -> báo lỗi conflict.
- Payment fail/timeout -> release slot theo TTL.

### UC-04: Đổi lịch khám
- Actor: Patient, Receptionist
- Tiền điều kiện: lịch hẹn ở trạng thái cho phép đổi.
- Luồng chính:
1. Kiểm tra policy đổi lịch.
2. Chọn slot mới.
3. Hủy giữ slot cũ, tạo/đổi sang slot mới.
4. Gửi thông báo thay đổi.
- Hậu điều kiện: lịch mới hợp lệ, lịch cũ cập nhật trạng thái.

### UC-05: Hủy lịch khám
- Actor: Patient, Receptionist
- Tiền điều kiện: lịch hẹn ở trạng thái cho phép hủy.
- Luồng chính:
1. Kiểm tra mốc thời gian và mức hoàn phí.
2. Cập nhật `CANCELLED`.
3. Billing Service xử lý hoàn tiền nếu có.
4. Gửi thông báo hủy.
- Hậu điều kiện: lịch hẹn hủy thành công.

### UC-06: Check-in và quản lý hàng đợi (Phase 2)
- Actor: Patient, Receptionist
- Tiền điều kiện: lịch hẹn hợp lệ trong ngày.
- Luồng chính:
1. Quét QR hoặc nhập mã lịch hẹn.
2. Queue Service cấp số thứ tự.
3. Trạng thái lịch chuyển `CHECKED_IN`.
4. Khi gọi khám -> `IN_PROGRESS`; kết thúc -> `COMPLETED`.
- Hậu điều kiện: luồng khám được ghi nhận đầy đủ.

### UC-07: Theo dõi KPI (Phase 2)
- Actor: Admin, Manager
- Luồng chính:
1. Reporting Service tổng hợp dữ liệu từ event/DB.
2. Trả dashboard KPI theo kỳ.
- Hậu điều kiện: có báo cáo vận hành.

## 8. Functional Requirements (chi tiết)
### 8.1 Phase 1
- FR-01 Identity:
- Đăng ký, đăng nhập, quên mật khẩu, refresh token.
- Hỗ trợ role: `PATIENT`, `DOCTOR`, `RECEPTIONIST`, `ADMIN`, `BILLING`.
- FR-02 Patient Profile:
- CRUD hồ sơ bệnh nhân và người phụ thuộc.
- Validate dữ liệu bắt buộc: họ tên, ngày sinh, số điện thoại.
- FR-03 Doctor/Schedule:
- Quản lý lịch theo bác sĩ, cơ sở, phòng, chuyên khoa.
- Tạo slot theo cấu hình thời lượng (ví dụ 15/20/30 phút).
- FR-04 Appointment:
- Giữ chỗ tạm thời khi thanh toán (TTL cấu hình, mặc định 10 phút).
- Chống double-booking bằng unique constraint + lock logic.
- FR-05 Reschedule/Cancel:
- Kiểm tra policy theo mốc thời gian trước giờ khám.
- Lưu lịch sử thay đổi trạng thái.
- FR-06 Payment:
- Tạo giao dịch, xác nhận callback từ gateway.
- Hỗ trợ trạng thái giao dịch: `INIT`, `SUCCESS`, `FAILED`, `REFUNDED`.
- FR-07 Notification:
- Gửi xác nhận đặt lịch ngay sau khi `BOOKED`.
- Gửi nhắc lịch trước giờ khám theo rule (ví dụ T-24h, T-2h).

### 8.2 Phase 2
- FR-08 Queue:
- Check-in bằng QR hoặc mã lịch hẹn.
- Tạo số thứ tự theo phòng khám và mức ưu tiên.
- FR-09 Reporting:
- Báo cáo đặt lịch thành công, no-show, thời gian chờ, tỉ lệ lấp đầy.
- FR-10 Audit:
- Ghi log thao tác quan trọng (đặt lịch, đổi/hủy, thanh toán, check-in).

## 9. Dữ liệu và ràng buộc
### 9.1 Entity chính
- `User(user_id, role, status, created_at, updated_at)`
- `Patient(patient_id, full_name, dob, phone, email, gender, address)`
- `Doctor(doctor_id, full_name, specialty_id, facility_id, status)`
- `Schedule(schedule_id, doctor_id, date, start_time, end_time)`
- `Slot(slot_id, schedule_id, start_time, end_time, status)`
- `Appointment(appointment_id, patient_id, doctor_id, slot_id, status, reason, created_at)`
- `Payment(payment_id, appointment_id, amount, method, status, txn_ref)`
- `QueueTicket(ticket_id, appointment_id, queue_no, status, checked_in_at)`
- `Notification(notification_id, channel, target, template, status, sent_at)`

### 9.2 Quy tắc dữ liệu
- Một `slot_id` chỉ có tối đa một `appointment` active (`BOOKED|CONFIRMED|CHECKED_IN|IN_PROGRESS`).
- `appointment.status` phải theo state machine hợp lệ.
- Mọi thay đổi trạng thái phải ghi audit trail.

## 10. State Machine
### 10.1 Appointment Status
`PENDING_PAYMENT -> BOOKED -> CONFIRMED -> CHECKED_IN -> IN_PROGRESS -> COMPLETED`

Nhánh phụ:
- `PENDING_PAYMENT -> CANCELLED`
- `BOOKED|CONFIRMED -> CANCELLED`
- `BOOKED|CONFIRMED -> NO_SHOW`

### 10.2 Payment Status
`INIT -> SUCCESS | FAILED`
- `SUCCESS -> REFUNDED` (khi thỏa điều kiện hoàn tiền)

## 11. API Requirements (mức hợp đồng)
### 11.1 Public APIs (qua Gateway)
- `POST /api/v1/auth/login`
- `POST /api/v1/auth/register`
- `GET /api/v1/doctors`
- `GET /api/v1/schedules/slots`
- `POST /api/v1/appointments`
- `PATCH /api/v1/appointments/{id}/reschedule`
- `PATCH /api/v1/appointments/{id}/cancel`
- (Phase 2) `POST /api/v1/appointments/{id}/check-in`
- (Phase 2) `GET /api/v1/reports/kpi`

### 11.2 Nguyên tắc API
- JSON UTF-8, version qua URI (`/v1`).
- Idempotency key cho API thanh toán/đặt lịch.
- Mã lỗi chuẩn: `400`, `401`, `403`, `404`, `409`, `422`, `500`.

## 12. Non-Functional Requirements
### 12.1 Security
- RBAC bắt buộc cho toàn bộ endpoint nghiệp vụ.
- TLS cho toàn bộ kết nối.
- Mã hóa dữ liệu nhạy cảm tại rest.
- Lưu audit log tối thiểu 12 tháng.

### 12.2 Performance
- P95 API đọc < 500ms.
- P95 API ghi < 800ms (không tính external gateway delay).
- Tối thiểu 300 concurrent users cho MVP.

### 12.3 Availability & Reliability
- Uptime mục tiêu 99.9% cho luồng đặt lịch.
- Retry và circuit breaker cho call liên service.
- Job retry cho notification thất bại.

### 12.4 Observability
- Correlation ID xuyên suốt request.
- Metrics: request count, error rate, latency, queue depth.
- Alert khi tỉ lệ lỗi > ngưỡng cấu hình.

## 13. Business Rules Mapping
- BR-01: Không double-booking slot.
- BR-02: Chính sách đổi/hủy theo deadline.
- BR-03: Chính sách hoàn tiền theo mốc thời gian.
- BR-04: Giới hạn đặt lịch cho bệnh nhân no-show quá ngưỡng.
- BR-05: Trạng thái lịch hẹn theo state machine chuẩn.

## 14. Acceptance Criteria (MVP)
- AC-01: Đặt lịch thành công trong trường hợp slot còn trống.
- AC-02: Nếu 2 người cùng đặt 1 slot, chỉ 1 request thành công, request còn lại nhận `409 Conflict`.
- AC-03: Hủy lịch trước deadline thực hiện hoàn tiền đúng rule.
- AC-04: Notification gửi xác nhận và nhắc lịch đúng thời điểm cấu hình.
- (Phase 2) AC-05: Check-in cập nhật đúng trạng thái và cấp số thứ tự.
- (Phase 2) AC-06: Dashboard hiển thị đầy đủ KPI đã định nghĩa.

## 15. Yêu cầu kiểm thử
- Unit test cho domain logic và state transition.
- Integration test cho luồng Appointment <-> Payment <-> Notification.
- Contract test cho API Gateway và service nội bộ.
- Load test cho luồng tìm slot và đặt lịch giờ cao điểm.
- UAT theo kịch bản Patient/Receptionist/Admin.

## 16. Ràng buộc triển khai
- Có thể triển khai container hóa qua Docker Compose cho môi trường dev.
- Cấu hình qua biến môi trường; không hard-code secret.
- Mỗi service có health check endpoint (`/health`).

## 17. Truy vết BRD -> SRS
- BRD FR-01 -> SRS FR-01, UC-01
- BRD FR-02 -> SRS FR-02
- BRD FR-03, FR-04 -> SRS FR-03, UC-02
- BRD FR-05, FR-06 -> SRS FR-04, FR-05, UC-03, UC-04, UC-05
- BRD FR-07 -> SRS FR-06
- BRD FR-08 -> SRS FR-07
- BRD FR-09 -> SRS FR-08 (Phase 2), UC-06 (Phase 2)
- BRD FR-10 -> SRS FR-09 (Phase 2), UC-07 (Phase 2)

## 18. Mục cần chốt trước khi dev
- Deadline đổi/hủy cụ thể (bao nhiêu giờ trước lịch khám).
- Tỷ lệ hoàn phí theo từng mốc.
- Ngưỡng no-show và cơ chế mở lại quyền đặt lịch.
- Danh sách cổng thanh toán và kênh thông báo áp dụng cho MVP.
- SLA nội bộ cho xử lý sự cố payment/notification.
