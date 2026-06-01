# BRD - MediBook (Hospital Appointment System)

## 1. Thông tin tài liệu
- Tên dự án: MediBook
- Loại hệ thống: Hospital Appointment System (Microservice Architecture)
- Phiên bản tài liệu: v1.0
- Ngày cập nhật: 2026-06-01
- Trạng thái: Draft for Review

## 2. Bối cảnh và vấn đề
- Bệnh nhân mất nhiều thời gian chờ đặt lịch và chờ khám.
- Lịch bác sĩ chưa tối ưu theo năng lực và khung giờ thực tế.
- Quy trình đổi/hủy lịch, nhắc lịch, check-in, thanh toán chưa thống nhất.
- Dữ liệu vận hành phân tán, khó đo lường chất lượng dịch vụ.

## 3. Mục tiêu nghiệp vụ
- Chuẩn hóa luồng đặt lịch khám từ online đến tại quầy.
- Giảm thời gian chờ khám trung bình.
- Tăng tỷ lệ lấp đầy lịch bác sĩ/phòng khám.
- Giảm tỷ lệ bệnh nhân không đến khám (no-show).
- Tăng tỷ lệ thanh toán online và mức độ hài lòng bệnh nhân.

## 4. Phạm vi dự án
### 4.1 In Scope (MVP)
- Quản lý tài khoản và phân quyền cơ bản.
- Quản lý hồ sơ bệnh nhân.
- Quản lý bác sĩ, chuyên khoa, lịch làm việc, khung giờ khám.
- Đặt lịch, đổi lịch, hủy lịch.
- Thanh toán cơ bản (online/offline).
- Nhắc lịch tự động qua SMS/Email/Zalo (tùy tích hợp).
- Check-in và điều phối trạng thái khám tại cơ sở.
- Báo cáo vận hành cốt lõi.

### 4.2 Out of Scope (giai đoạn sau)
- Tư vấn từ xa (telemedicine).
- Tích hợp bảo hiểm nâng cao.
- AI gợi ý lịch/đề xuất điều trị.
- Quản lý bệnh án nâng cao liên viện.

## 5. Đối tượng sử dụng
- Bệnh nhân/Người nhà
- Bác sĩ
- Lễ tân/Điều phối viên
- Quản trị bệnh viện
- Kế toán/Billing
- Đối tác tích hợp (SMS gateway, payment gateway)

## 6. Mô hình nghiệp vụ theo microservice
- Identity Service: xác thực, phân quyền, quản lý phiên.
- Patient Profile Service: hồ sơ bệnh nhân, người thân, thông tin y tế nền.
- Doctor & Schedule Service: thông tin bác sĩ, ca làm việc, slot khám.
- Appointment Service: tạo/đổi/hủy lịch, trạng thái lịch hẹn.
- Queue Service: check-in, phát số thứ tự, điều phối luồng khám.
- Billing/Payment Service: phí khám, giao dịch, hoàn tiền.
- Notification Service: nhắc hẹn, thông báo thay đổi lịch.
- Reporting Service: KPI vận hành, dashboard quản trị.
- API Gateway/BFF: cổng truy cập cho app bệnh nhân và portal nội bộ.

## 7. Quy trình nghiệp vụ chính
### 7.1 Đặt lịch khám
1. Bệnh nhân đăng nhập hoặc tạo tài khoản.
2. Chọn cơ sở/chuyên khoa/bác sĩ.
3. Hệ thống hiển thị slot khả dụng theo thời gian thực.
4. Bệnh nhân xác nhận lịch và phương thức thanh toán.
5. Hệ thống tạo lịch ở trạng thái `BOOKED`.
6. Notification Service gửi xác nhận đặt lịch.

### 7.2 Đổi lịch khám
1. Bệnh nhân chọn lịch cần đổi.
2. Hệ thống kiểm tra chính sách đổi lịch theo thời gian.
3. Bệnh nhân chọn slot mới.
4. Hệ thống cập nhật trạng thái lịch cũ và lịch mới.
5. Gửi thông báo cập nhật lịch.

### 7.3 Hủy lịch khám
1. Bệnh nhân/lễ tân gửi yêu cầu hủy.
2. Hệ thống kiểm tra điều kiện hủy (deadline, phí hủy nếu có).
3. Cập nhật trạng thái `CANCELLED`.
4. Payment Service xử lý hoàn tiền (nếu đủ điều kiện).
5. Gửi thông báo xác nhận hủy.

### 7.4 Check-in và khám
1. Bệnh nhân đến cơ sở, check-in bằng mã lịch hẹn/QR.
2. Queue Service cấp số thứ tự và trạng thái `CHECKED_IN`.
3. Điều phối viên gọi khám theo thứ tự và ưu tiên nghiệp vụ.
4. Kết thúc khám, cập nhật trạng thái `COMPLETED`.
5. Tạo lịch tái khám nếu có chỉ định.

## 8. Quy tắc nghiệp vụ cốt lõi
- Mỗi slot khám chỉ được gán cho một lịch hẹn đã xác nhận.
- Không cho phép đổi/hủy lịch sau mốc thời gian cấu hình (ví dụ trước giờ khám 2 giờ).
- Chính sách hoàn tiền theo điều kiện:
- Hoàn 100% nếu hủy trước ngưỡng T1.
- Hoàn X% nếu hủy trong ngưỡng T2.
- Không hoàn nếu quá hạn.
- Bệnh nhân no-show quá N lần có thể bị giới hạn đặt lịch trước.
- Trạng thái lịch hẹn chuẩn:
- `PENDING_PAYMENT`
- `BOOKED`
- `CONFIRMED`
- `CHECKED_IN`
- `IN_PROGRESS`
- `COMPLETED`
- `CANCELLED`
- `NO_SHOW`

## 9. Yêu cầu chức năng (Functional Requirements)
- FR-01: Đăng ký/đăng nhập, quên mật khẩu, phân quyền vai trò.
- FR-02: Quản lý hồ sơ bệnh nhân và người phụ thuộc.
- FR-03: Tìm kiếm bác sĩ/chuyên khoa/cơ sở theo bộ lọc.
- FR-04: Đồng bộ lịch trống theo thời gian thực.
- FR-05: Tạo lịch hẹn, giữ chỗ tạm thời khi đang thanh toán.
- FR-06: Đổi/hủy lịch theo chính sách cấu hình.
- FR-07: Hỗ trợ thanh toán online/offline.
- FR-08: Nhắc lịch tự động trước thời điểm khám.
- FR-09: Check-in tại quầy bằng mã lịch hẹn/QR.
- FR-10: Dashboard báo cáo KPI vận hành.

## 10. Yêu cầu phi chức năng (Non-Functional Requirements)
- Bảo mật:
- Áp dụng RBAC cho toàn bộ API.
- Mã hóa dữ liệu nhạy cảm khi lưu trữ và truyền tải.
- Audit log cho thao tác quan trọng.
- Hiệu năng:
- Hệ thống đáp ứng tối thiểu 95% request dưới 500ms (API đọc).
- Hỗ trợ mở rộng ngang cho Appointment, Schedule, Notification.
- Sẵn sàng:
- Uptime mục tiêu >= 99.9% cho chức năng đặt lịch.
- Triển khai có cơ chế retry/circuit breaker cho gọi liên service.
- Quan sát hệ thống:
- Centralized logging, metrics, tracing.
- Cảnh báo khi tăng lỗi thanh toán, lỗi đặt lịch, lỗi gửi thông báo.

## 11. Báo cáo và KPI
- Tỷ lệ đặt lịch thành công.
- Tỷ lệ no-show theo bác sĩ/chuyên khoa/khung giờ.
- Thời gian chờ trung bình từ check-in đến vào khám.
- Tỷ lệ lấp đầy lịch bác sĩ.
- Tỷ lệ thanh toán online thành công.
- Tỷ lệ hủy/đổi lịch.
- Tỷ lệ bệnh nhân quay lại trong 30/60/90 ngày.

## 12. Tích hợp bên thứ ba
- Payment Gateway (VNPay/MoMo/Stripe hoặc tùy chọn triển khai).
- SMS/Email/Zalo notification provider.
- Hệ thống HIS/EMR nội bộ bệnh viện (phase 2).

## 13. Rủi ro và giả định
### 13.1 Rủi ro
- Dữ liệu lịch bác sĩ thay đổi thường xuyên gây xung đột slot.
- Sai lệch trạng thái khi thanh toán thất bại nhưng đã giữ slot.
- Chậm trễ gửi thông báo làm giảm trải nghiệm bệnh nhân.
- Tích hợp hệ thống cũ (legacy HIS) có độ trễ và thiếu chuẩn dữ liệu.

### 13.2 Giả định
- Cơ sở khám có dữ liệu bác sĩ/chuyên khoa chuẩn hóa ban đầu.
- Chính sách đổi/hủy/hoàn tiền được thống nhất từ nghiệp vụ.
- Có đầu mối vận hành để xác nhận quy trình check-in tại quầy.

## 14. Lộ trình triển khai đề xuất
### Phase 1 (8-12 tuần)
- Identity, Patient Profile, Doctor & Schedule, Appointment.
- Notification và Payment cơ bản.
- Portal bệnh nhân (đặt lịch, quản lý lịch hẹn).

### Phase 2
- Queue Service và luồng check-in tại cơ sở.
- Reporting dashboard nâng cao.
- Tích hợp HIS/EMR mức cơ bản.

### Phase 3
- Tối ưu vận hành bằng rule engine.
- Tích hợp bảo hiểm và telemedicine.
- AI gợi ý lịch và dự đoán no-show.

## 15. Tiêu chí nghiệm thu MVP
- Bệnh nhân có thể đặt/đổi/hủy lịch thành công end-to-end.
- Đồng bộ chính xác slot khám trong điều kiện tải thực tế.
- Nhắc lịch tự động hoạt động ổn định.
- Thanh toán ghi nhận đúng trạng thái và có cơ chế hoàn tiền cơ bản.
- Check-in cập nhật đúng trạng thái lịch và luồng xếp hàng.
- Dashboard hiển thị đầy đủ KPI cốt lõi.

## 16. Phụ lục: Danh sách quyết định cần chốt sớm
- Chính sách phí hủy/đổi lịch chi tiết theo thời gian.
- Mô hình overbooking có áp dụng hay không.
- Quy tắc ưu tiên bệnh nhân đặc biệt (người cao tuổi, cấp cứu, VIP).
- Danh mục trạng thái chuẩn dùng xuyên suốt các service.
- SLA xử lý sự cố khi tích hợp payment/notification thất bại.
