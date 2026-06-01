# Coding Convention - MediBook

## 1. Mục tiêu
- Chuẩn hóa cấu trúc source code cho toàn bộ microservice của MediBook.
- Giảm khác biệt cách tổ chức code giữa các team.
- Tăng tốc review, onboarding và bảo trì.

## 2. Cấu trúc package bắt buộc
Mỗi module/service phải có đầy đủ 15 package sau:

1. `controller`
2. `service`
3. `repository`
4. `entity`
5. `dto`
6. `mapper`
7. `exception`
8. `enums`
9. `config`
10. `client`
11. `event`
12. `security`
13. `scheduler`
14. `util`
15. `constant`

## 3. Trách nhiệm từng package
- `controller`: expose REST API, validate input mức API, không chứa business logic.
- `service`: xử lý nghiệp vụ, orchestration giữa repository/client/event.
- `repository`: truy cập dữ liệu (JPA/MyBatis...), query DB.
- `entity`: model persistence map với DB table.
- `dto`: object cho request/response hoặc service contract.
- `mapper`: mapping giữa entity <-> dto <-> domain model (nếu có).
- `exception`: custom exception, global exception handler, error response model.
- `enums`: enum nghiệp vụ (status, type, channel...).
- `config`: bean config, datasource, message broker, OpenAPI, Jackson, etc.
- `client`: client gọi service ngoài hoặc service nội bộ (Feign/WebClient/RestTemplate).
- `event`: event model, producer, consumer, handler.
- `security`: auth filter, token util, RBAC components, security config.
- `scheduler`: cron job, retry job, cleanup job.
- `util`: helper dùng chung, stateless, không chứa business rule.
- `constant`: hằng số hệ thống, key, regex, message code.

## 4. Nguyên tắc bắt buộc
- Không viết business logic trong `controller`, `repository`, `util`.
- `service` là nơi duy nhất điều phối nghiệp vụ.
- `util` không được inject repository/client.
- `constant` chỉ chứa constant, không chứa method nghiệp vụ.
- `mapper` không gọi network/DB.
- Mọi exception nghiệp vụ phải định nghĩa trong `exception` và map về mã lỗi chuẩn.

## 5. Quy tắc đặt tên
- Class:
- Controller: `XxxController`
- Service: `XxxService` / `XxxServiceImpl`
- Repository: `XxxRepository`
- Entity: danh từ số ít, ví dụ `AppointmentEntity`
- DTO: `XxxRequest`, `XxxResponse`, `XxxDto`
- Mapper: `XxxMapper`
- Exception: `XxxException`
- Enum: `XxxStatus`, `XxxType`
- Constant: `XxxConstants`
- File/package dùng `lowercase`, không dùng dấu gạch nối.

## 6. Cấu trúc thư mục chuẩn
```text
src/main/java/com/medibook/<service-name>/
  controller/
  service/
  repository/
  entity/
  dto/
  mapper/
  exception/
  enums/
  config/
  client/
  event/
  security/
  scheduler/
  util/
  constant/
```

## 7. Quy ước luồng xử lý
1. `controller` nhận request và gọi `service`.
2. `service` validate business rule, thao tác `repository`.
3. `service` gọi `client` nếu cần tích hợp ngoài.
4. `service` publish `event` khi có thay đổi trạng thái quan trọng.
5. `mapper` thực hiện transform object ở các điểm vào/ra.

## 8. Quy tắc cho scheduler và event
- `scheduler` chỉ trigger workflow; business logic nằm ở `service`.
- `event` consumer phải idempotent.
- Event thất bại phải có retry + dead-letter strategy (theo thiết kế service).

## 9. Quy tắc bảo mật
- Tất cả endpoint qua `security` filter chain.
- Kiểm tra role/permission tại service layer cho action nhạy cảm.
- Không log dữ liệu nhạy cảm (PII, token, password, otp).

## 10. Quy tắc exception và mã lỗi
- Mỗi lỗi nghiệp vụ có `errorCode` rõ ràng.
- Mapping lỗi thống nhất qua global handler.
- Trả response lỗi theo format chung của dự án:
- `code`
- `message`
- `traceId`
- `details` (nếu có)

## 11. Quy tắc code review theo convention
- PR bị reject nếu thiếu package bắt buộc hoặc đặt sai trách nhiệm.
- PR bị reject nếu business logic nằm trong `controller` hoặc `util`.
- PR bị reject nếu tạo constant rải rác ngoài package `constant`.
- PR bị reject nếu thêm enum/status trực tiếp dạng string hard-code.

## 12. Áp dụng và hiệu lực
- Áp dụng cho tất cả service mới từ ngày 2026-06-01.
- Service cũ phải refactor dần theo từng sprint, ưu tiên module thay đổi nhiều.
- Mọi exception so với convention phải được thống nhất trong review và ghi chú rõ trong PR.
