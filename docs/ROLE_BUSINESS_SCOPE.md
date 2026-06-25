# MediBook Role Business Scope

## Bối cảnh

MediBook hướng tới mô hình phòng khám tư / khám dịch vụ, không phải hệ thống HIS đầy đủ cho bệnh viện lớn ngay từ đầu. Vì vậy nghiệp vụ nên được giữ gọn quanh các luồng chính: đặt lịch, tiếp nhận, khám bệnh, thanh toán cơ bản và quản trị vận hành.

Tài liệu này mô tả phạm vi nghiệp vụ theo từng role để làm cơ sở thiết kế UI, API, phân quyền và backlog.

## Nguyên tắc phân quyền

- Frontend có thể dùng role để điều chỉnh giao diện, navigation và trải nghiệm người dùng.
- Backend luôn là nơi kiểm tra quyền thật sự trước khi thực hiện nghiệp vụ.
- Một người dùng có thể có nhiều role, nhưng mỗi thời điểm frontend sẽ có một `activeRole` để hiển thị UI phù hợp.
- Role mặc định của user do backend quyết định thông qua `user_roles.is_default` và trả về trong `UserPrivateResponse.defaultRole`.
- Không nên để một role nhìn thấy hoặc sửa dữ liệu không cần thiết cho nghiệp vụ của họ.

## Bệnh Nhân

Bệnh nhân là người đặt lịch, quản lý hồ sơ cá nhân/người thân và theo dõi thông tin sau khám.

### Được làm

- Đăng ký và đăng nhập tài khoản.
- Quản lý hồ sơ cá nhân.
- Tạo và quản lý hồ sơ người thân như con, bố mẹ, vợ/chồng.
- Đặt lịch khám dịch vụ.
- Chọn chuyên khoa, bác sĩ hoặc khung giờ nếu phòng khám cho phép.
- Xem lịch hẹn sắp tới.
- Hủy hoặc đổi lịch trong điều kiện cho phép.
- Theo dõi trạng thái lịch hẹn: chờ xác nhận, đã xác nhận, đã check-in, đã khám xong.
- Xem lịch sử khám.
- Xem đơn thuốc.
- Xem kết quả xét nghiệm hoặc file đính kèm.
- Nhận thông báo về lịch hẹn, nhắc tái khám, kết quả đã có.
- Thanh toán online nếu hệ thống tích hợp thanh toán ở giai đoạn sau.

### Không nên làm

- Tự sửa dữ liệu y tế đã được bác sĩ ghi nhận.
- Xem thông tin vận hành nội bộ của phòng khám.
- Xem toàn bộ lịch chi tiết của bác sĩ nếu nghiệp vụ đặt lịch không cần.

### Dashboard nên có

- Lịch hẹn sắp tới.
- Hồ sơ cá nhân và người thân.
- Kết quả mới.
- Lịch sử khám gần đây.
- Nhắc tái khám.

## Lễ Tân

Lễ tân là người vận hành lịch hẹn, tiếp nhận bệnh nhân tại quầy và điều phối hàng đợi khám.

### Được làm

- Xem dashboard vận hành trong ngày.
- Tạo lịch hẹn cho bệnh nhân gọi điện hoặc đến trực tiếp.
- Tìm kiếm bệnh nhân.
- Tạo hồ sơ bệnh nhân nếu bệnh nhân chưa có tài khoản.
- Cập nhật thông tin hành chính: họ tên, số điện thoại, ngày sinh, giới tính, địa chỉ, số BHYT nếu có.
- Gán lịch hẹn cho bác sĩ, chuyên khoa hoặc dịch vụ.
- Xác nhận lịch hẹn.
- Đổi lịch hoặc hủy lịch.
- Check-in bệnh nhân khi đến phòng khám.
- Quản lý hàng đợi khám.
- Gọi số tiếp theo hoặc chuyển trạng thái chờ khám.
- Chuyển bệnh nhân vào phòng khám/bác sĩ.
- Ghi chú hành chính như đến muộn, cần hỗ trợ, ưu tiên.
- Tạo phiếu thu hoặc xác nhận thanh toán nếu có module thanh toán.
- In phiếu khám, phiếu hẹn, hóa đơn cơ bản nếu cần.

### Không nên làm

- Xem sâu bệnh án chuyên môn nếu không cần cho tiếp nhận.
- Sửa chẩn đoán, đơn thuốc hoặc chỉ định của bác sĩ.
- Xem dữ liệu quản trị hệ thống.
- Thay đổi phân quyền người dùng.

### Dashboard nên có

- Tổng lịch hẹn hôm nay.
- Bệnh nhân chờ check-in.
- Hàng đợi khám trực tiếp.
- Lịch bác sĩ trong ngày.
- Thanh toán chờ xử lý.
- Tạo lịch nhanh.
- Check-in nhanh.

## Bác Sĩ

Bác sĩ là người thực hiện khám và tạo dữ liệu chuyên môn.

### Được làm

- Xem danh sách bệnh nhân được phân cho mình trong ngày.
- Xem hàng đợi khám của mình.
- Bắt đầu ca khám.
- Xem hồ sơ bệnh nhân cần thiết.
- Xem lịch sử khám trước đó.
- Ghi nhận triệu chứng và lý do khám.
- Ghi sinh hiệu nếu phòng khám không có điều dưỡng riêng.
- Ghi chẩn đoán.
- Ghi kết luận khám.
- Kê đơn thuốc.
- Chỉ định xét nghiệm hoặc cận lâm sàng nếu có.
- Xem kết quả xét nghiệm.
- Ghi lời dặn.
- Hẹn tái khám.
- Hoàn tất ca khám.
- Tạo hoặc in đơn thuốc, phiếu kết quả, giấy hẹn.
- Gửi thông tin sau khám cho bệnh nhân.

### Không nên làm

- Tự sửa thanh toán nếu không có role phù hợp.
- Quản lý tài khoản nhân sự.
- Xóa hồ sơ bệnh nhân.
- Xem dashboard tài chính/quản trị nếu không có role admin.

### Dashboard nên có

- Bệnh nhân chờ khám của tôi.
- Ca đang khám hiện tại.
- Kết quả cần xem.
- Hồ sơ chưa hoàn tất.
- Lịch làm việc hôm nay.
- Truy cập nhanh: mở bệnh án, kê đơn, chỉ định xét nghiệm, tạo giấy hẹn.

## Admin

Admin là người cấu hình hệ thống và quản trị vận hành phòng khám.

### Được làm

- Quản lý tài khoản nhân sự.
- Gán role: bác sĩ, lễ tân, admin.
- Quản lý danh sách bác sĩ.
- Quản lý chuyên khoa.
- Quản lý dịch vụ khám.
- Quản lý lịch làm việc bác sĩ.
- Quản lý phòng khám hoặc phòng chức năng nếu có.
- Quản lý cấu hình khung giờ đặt lịch.
- Quản lý giá dịch vụ.
- Xem báo cáo tổng quan: số lịch hẹn, số lượt khám, doanh thu, tỷ lệ hủy lịch, hiệu suất bác sĩ.
- Xem audit/log hoạt động quan trọng.
- Quản lý mẫu thông báo.
- Quản lý cấu hình hệ thống.

### Không nên làm

- Mặc định làm thay bác sĩ về chuyên môn.
- Sửa bệnh án, đơn thuốc hoặc kết luận khám nếu admin không đồng thời có role bác sĩ.
- Thực hiện thao tác nghiệp vụ thay role khác nếu không được cấp role tương ứng.

### Dashboard nên có

- Tổng quan vận hành.
- Doanh thu.
- Lịch hẹn.
- Nhân sự.
- Cấu hình dịch vụ.
- Báo cáo.
- Cảnh báo hệ thống hoặc dữ liệu bất thường.

## Luồng Nghiệp Vụ Chính

1. Bệnh nhân đặt lịch online hoặc gọi điện đến phòng khám.
2. Lễ tân xác nhận lịch hẹn.
3. Bệnh nhân đến phòng khám.
4. Lễ tân check-in bệnh nhân.
5. Bệnh nhân vào hàng đợi khám.
6. Bác sĩ bắt đầu ca khám.
7. Bác sĩ ghi kết luận, đơn thuốc, chỉ định nếu có.
8. Bác sĩ hoàn tất ca khám.
9. Lễ tân xử lý thanh toán hoặc in giấy tờ nếu cần.
10. Bệnh nhân xem lại kết quả, đơn thuốc và lịch sử khám trên web/app.

## Thứ Tự Triển Khai Đề Xuất

1. Patient profile và patient relationship.
2. Appointment booking.
3. Receptionist appointment, check-in và queue.
4. Doctor encounter, medical record và prescription.
5. Payment basic.
6. Admin configuration và report.

## Ghi Chú Thiết Kế UI

Các dashboard theo role nên dùng chung những thành phần nền tảng:

- `DashboardShell`: sidebar, topbar, avatar menu, search, layout wrapper.
- `DashboardStatCardComponent`: KPI/stat card.
- `StatusBadgeComponent`: badge trạng thái dùng cho lịch hẹn, hàng đợi, thanh toán, ca khám.
- `DashboardPanelComponent`: panel có header/action/content.
- `DataTableShellComponent`: khung bảng có header, loading, empty state, pagination nếu cần.
- `QuickActionButtonComponent`: action nhanh theo role.

Phần riêng theo role nên nằm trong từng feature:

- Patient: appointment summary, dependent profiles, health record summary.
- Receptionist: appointment intake, check-in queue, payment waiting list.
- Doctor: doctor queue, active encounter, pending results.
- Admin: operational metrics, staff/service configuration, reports.
