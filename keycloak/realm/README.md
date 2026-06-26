# MediBook Keycloak Realm Mapping

File `medibook-realm.json` là cấu hình static cho Keycloak realm local/dev của MediBook.

JSON không hỗ trợ comment, nên file này giải thích ý nghĩa từng phần cấu hình.

## 1. Realm

```json
"realm": "medibook"
```

`medibook` là realm riêng cho ứng dụng MediBook. Không dùng realm `master` cho business app.

Các thiết lập chính:

- `enabled: true`: bật realm.
- `displayName: MediBook`: tên hiển thị.
- `accessTokenLifespan: 1800`: access token sống 30 phút.
- `ssoSessionIdleTimeout: 1800`: session idle timeout 30 phút.
- `ssoSessionMaxLifespan: 36000`: session tối đa 10 giờ.
- `bruteForceProtected: true`: bật chống brute force.
- `failureFactor: 5`: khóa/tạm chặn sau 5 lần sai theo policy Keycloak.

## 2. Realm Roles

Realm roles đang khai báo theo role nghiệp vụ hiện có của MediBook:

- `PATIENT`
- `RECEPTIONIST`
- `DOCTOR`
- `ADMIN`

Lý do giữ các role này trong Keycloak:

- Token có thể chứa role để gateway/services kiểm tra quyền cơ bản.
- Đồng bộ với bảng nội bộ `roles` của `identity-service`.
- Giảm thay đổi ở code hiện tại, đặc biệt `patient-service` đang đọc claim `roles`.

## 3. Client Roles Cho Service-To-Service

Các role trong `roles.client` là API-level permissions cho internal calls.

Ví dụ:

- `notification-service/notification:send`
- `appointment-service/appointment:read`
- `appointment-service/appointment:write`
- `patient-service/patient:read`
- `audit-service/audit:write`

Các role này không thay thế business authorization.

Ý nghĩa:

```text
Service token scope/client role = service này có được gọi API nội bộ kia không.
Business permission = user này có được thực hiện action nghiệp vụ này không.
```

## 4. Client `identity-service`

`identity-service` là auth facade cho custom login UI.

Cấu hình quan trọng:

- `publicClient: false`: đây là backend client, có secret.
- `secret: identity-service-secret`: secret local/dev.
- `directAccessGrantsEnabled: true`: cho phép login bằng username/password qua token endpoint.
- `serviceAccountsEnabled: true`: cho phép `identity-service` lấy service token nếu cần gọi Admin API hoặc API nội bộ.
- `standardFlowEnabled: false`: không dùng browser redirect flow cho client này.

Flow dùng client này:

```text
FE custom login UI
  -> POST /api/v1/auth/login
  -> identity-service
  -> Keycloak token endpoint grant_type=password
  -> Keycloak trả access_token + refresh_token
  -> identity-service trả token cho FE
```

## 5. Client `medibook-web`

`medibook-web` là public frontend client để chuẩn bị cho Authorization Code + PKCE nếu sau này cần.

Hiện tại flow chính vẫn là FE gọi `identity-service`, nhưng tạo client này để không khóa kiến trúc.

Cấu hình:

- `publicClient: true`: frontend không giữ client secret.
- `standardFlowEnabled: true`: cho phép Authorization Code flow.
- `directAccessGrantsEnabled: false`: FE không gọi password grant trực tiếp.
- `redirectUris: http://localhost:*/*`: phục vụ local dev.
- `webOrigins: *`: phục vụ local dev.

## 6. Service Clients

Các service clients:

- `api-gateway`
- `patient-service`
- `doctor-schedule-service`
- `appointment-service`
- `payment-service`
- `queue-service`
- `notification-service`
- `reporting-service`
- `audit-service`

Cấu hình chung:

- `publicClient: false`
- `clientAuthenticatorType: client-secret`
- `serviceAccountsEnabled: true`
- `standardFlowEnabled: false`
- `directAccessGrantsEnabled: false`

Ý nghĩa:

```text
Service A dùng client_id/client_secret để xin token bằng client_credentials.
Service A gọi Service B qua internal DNS.
Service B verify token qua Keycloak issuer/JWK.
```

## 7. Protocol Mapper `publicId`

Mapper `publicId` lấy user attribute Keycloak tên `publicId` và đưa vào token claim `publicId`.

Realm JSON cũng khai báo User Profile attributes `publicId` và `fullName` để Keycloak 26 cho phép lưu metadata nội bộ trên user. Các attribute này admin-edit, user-view:

```text
User Profile attribute publicId
  view: admin, user
  edit: admin

User Profile attribute fullName
  view: admin, user
  edit: admin
```

`firstName` và `lastName` vẫn là field built-in của Keycloak nhưng không required trong realm này. MediBook dùng `users.full_name` làm nguồn dữ liệu chính và đồng bộ sang attribute `fullName`.

MediBook hiện dùng `users.public_id` làm định danh public UUID. Khi `identity-service` register user, service cần set attribute này vào Keycloak user:

```text
Keycloak user attribute publicId = users.public_id
```

Token sau login sẽ có:

```json
"publicId": "..."
```

Điều này giúp code hiện tại như `patient-service` tiếp tục đọc được:

```java
jwt.getClaimAsString("publicId")
```

## 8. Protocol Mapper `roles`

Mapper `app roles as roles` đưa user attribute `appRoles` vào claim flat `roles`.

Không map trực tiếp realm roles vào claim `roles` vì Keycloak realm roles có cả role kỹ thuật như `default-roles-medibook`, `offline_access`, `uma_authorization`. Claim `roles` của MediBook chỉ chứa role nghiệp vụ từ DB nội bộ, ví dụ `PATIENT`.

Token sẽ có dạng:

```json
"roles": ["PATIENT"]
```

Điều này giữ tương thích với code hiện tại:

```java
jwt.getClaimAsStringList("roles")
```

Nếu sau này muốn dùng format chuẩn Keycloak, có thể đọc roles từ:

```json
"realm_access": {
  "roles": ["default-roles-medibook", "offline_access", "PATIENT", "uma_authorization"]
}
```

hoặc:

```json
"resource_access": {
  "some-client": {
    "roles": ["..."]
  }
}
```

## 9. Secrets Trong File JSON

Các secret trong file JSON là secret local/dev, ví dụ:

- `identity-service-secret`
- `patient-service-secret`
- `appointment-service-secret`

Không dùng các secret này cho production.

Production nên quản lý secret bằng:

- Kubernetes Secret
- Vault
- CI/CD secret manager
- hoặc quy trình quản trị Keycloak riêng.

## 10. Import Behavior

Keycloak chỉ import realm ổn định nhất khi realm chưa tồn tại trong DB.

Nếu realm `medibook` đã tồn tại, việc sửa JSON rồi restart container có thể không cập nhật toàn bộ cấu hình như kỳ vọng.

Với local/dev, để test import sạch có thể reset volume Keycloak:

```bash
docker compose down
docker volume rm medibook_pg_keycloak_data
docker compose up -d postgres-keycloak keycloak
```

Cẩn thận: lệnh xóa volume sẽ xóa toàn bộ DB Keycloak local.

## 11. User Không Nằm Trong Realm JSON

File realm JSON không chứa user thật.

User runtime nên được tạo qua:

```text
POST /api/v1/auth/register
```

Sau refactor, `identity-service` sẽ:

1. Tạo user trong Keycloak.
2. Tạo user profile trong DB nội bộ.
3. Set attribute `publicId` trong Keycloak.
4. Tạo mapping `external_identities`.
5. Assign role nội bộ và role Keycloak.

## 12. Cách Test Nhanh Sau Khi Import

OpenID metadata:

```bash
curl http://localhost:8180/realms/medibook/.well-known/openid-configuration
```

Token endpoint với client `identity-service`:

```bash
curl -X POST http://localhost:8180/realms/medibook/protocol/openid-connect/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=password" \
  -d "client_id=identity-service" \
  -d "client_secret=identity-service-secret" \
  -d "username=<email>" \
  -d "password=<password>"
```

Lệnh trên cần user đã tồn tại trong realm `medibook`.
