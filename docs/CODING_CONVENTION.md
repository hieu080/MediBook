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

## 12. Quy tắc Git
### 12.1 Nguyên tắc chung
- Mỗi commit chỉ nên xử lý một nhóm thay đổi có cùng mục tiêu.
- Không commit secret, private key, token, password, file `.env`, file `.pem`, dữ liệu cá nhân thật hoặc dữ liệu production.
- Không commit file build/generated không cần thiết như `target/`, `build/`, `out/`, log runtime hoặc file IDE cá nhân.
- Trước khi commit phải kiểm tra `git status` và `git diff` để chắc chắn chỉ đưa đúng file cần thiết.
- Không sửa, format hoặc revert file ngoài phạm vi task nếu không có lý do rõ ràng.
- Không rewrite history nhánh dùng chung bằng force push nếu chưa được thống nhất.

### 12.2 Quy tắc đặt tên branch
Format branch:

```text
<type>/<owner>_<scope>_<short-description>
```

Trong đó:
- `type`: `feature`, `fix`, `refactor`, `docs`, `test`, `chore`, `hotfix`.
- `owner`: tên người làm hoặc mã nhóm, viết lowercase, không dấu.
- `scope`: epic, use case hoặc service liên quan, ví dụ `uc01`, `uc09`, `identity`, `appointment`.
- `short-description`: mô tả ngắn bằng kebab-case hoặc snake_case, không dấu.

Ví dụ:
- `feature/hieu_uc03_patient-profile`
- `feature/team1_uc09_create-appointment`
- `fix/anh_identity_refresh-token`
- `docs/hieu_project-progress-checklist`
- `refactor/team2_patient-relationship-repository`

### 12.3 Quy tắc commit message
Format commit:

```text
<type>: [<scope>] <short summary>
```

Trong đó:
- `type`: `feature`, `fix`, `refactor`, `docs`, `test`, `chore`, `config`, `migration`.
- `scope`: epic/use case/service/module, ví dụ `UC-01`, `UC-09`, `identity-service`, `patient-service`.
- `short summary`: mô tả ngắn, rõ kết quả thay đổi, có thể dùng tiếng Việt không dấu hoặc tiếng Anh thống nhất theo team.

Ví dụ:
- `feature: [UC-01] Bo sung API dang nhap va refresh token`
- `feature: [UC-09] Tao appointment voi idempotency key`
- `fix: [identity-service] Chan refresh token da bi revoke`
- `migration: [doctor-schedule-service] Tao bang schedules va slots`
- `docs: [project] Bo sung checklist tien do use case`
- `test: [appointment-service] Bo sung test double booking cung slot`

Commit message nen neu ket qua, khong nen ghi chung chung:
- Nen: `fix: [payment-service] Xu ly callback lap khong doi trang thai SUCCESS`
- Khong nen: `fix bug`
- Khong nen: `update code`

### 12.4 Quy tắc Pull Request
PR title nen theo format:

```text
<type>: [<scope>] <short summary>
```

PR description bat buoc co:
- Muc tieu thay doi.
- Use case lien quan, ví dụ `UC-03` hoặc `UC-09`.
- API thay doi, neu co.
- Migration DB, neu co.
- Test da chay.
- Anh huong toi service khac, neu co.
- Viec chua lam hoặc known limitation, neu co.

Checklist PR:
- Code dung package convention.
- Controller khong chua business logic.
- Service chua business rule va transaction boundary.
- API co validation request.
- Endpoint protected co security/authorization phu hop.
- Error code duoc map ro rang.
- Migration moi khong sua migration da chay truoc do.
- Co test cho happy path va loi quan trong.
- README/docs/API contract/checklist duoc cap nhat neu thay doi behavior hoac endpoint.

### 12.5 Quy tắc migration trong Git
- Khong sua noi dung migration da merge vao nhánh dùng chung hoặc da chay tren DB cua team.
- Neu can thay doi schema, tao migration moi theo version tiep theo.
- Ten migration phai ro nghiep vu:
- `V1__init.sql`
- `V2__add_doctor_role.sql`
- `V3__add_default_role_to_user_roles.sql`
- `V4__create_slots_table.sql`

### 12.6 Quy tắc review va merge
- PR phai duoc review truoc khi merge vao nhánh dùng chung.
- Reviewer uu tien tim loi nghiep vu, loi security, race condition, duplicate data va test thieu.
- Khong merge khi build/test bat buoc dang fail, tru khi team ghi ro exception.
- Khong merge code co secret hoac key that.
- Khong merge thay doi lam sai API contract neu chua cap nhat docs lien quan.

## 13. Áp dụng và hiệu lực
- Áp dụng cho tất cả service mới từ ngày 2026-06-01.
- Service cũ phải refactor dần theo từng sprint, ưu tiên module thay đổi nhiều.
- Mọi exception so với convention phải được thống nhất trong review và ghi chú rõ trong PR.
