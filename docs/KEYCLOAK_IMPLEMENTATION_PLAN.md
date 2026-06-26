# Keycloak Implementation Plan

## 1. Mục Tiêu

Tài liệu này mô tả kế hoạch refactor MediBook từ mô hình `identity-service` tự phát hành JWT/RSA sang mô hình dùng Keycloak làm Authorization Server.

Mục tiêu chính:

- Pin một version Keycloak cụ thể, không dùng `latest`.
- Keycloak dùng PostgreSQL riêng.
- Giữ login UI riêng của MediBook qua `identity-service` auth facade.
- Giữ mô hình stateless ở app backend: FE giữ token, backend không lưu login session.
- Đồng bộ user nội bộ giữa Keycloak và DB của `identity-service`.
- Gateway verify JWT từ Keycloak.
- Service-to-service dùng Keycloak `client_credentials` qua internal DNS.
- Business authorization vẫn nằm ở service nghiệp vụ hoặc permission layer tương ứng, không nhét business matrix vào Keycloak.

## 2. Kiến Trúc Target

```text
                    +----------------------+
                    |       Keycloak       |
                    |----------------------|
                    | verify password      |
                    | refresh token        |
                    | issue user token     |
                    | issue service token  |
                    | RSA private key      |
                    | JWK endpoint         |
                    | service clients      |
                    | PostgreSQL riêng     |
                    +----------+-----------+
                               |
                               | issuer-uri / JWK
                               v

                    +----------------------+
                    | identity-service     |
                    |----------------------|
                    | auth facade          |
                    | custom login API     |
                    | call Keycloak token  |
                    | sync internal user   |
                    | no server session    |
                    | return token to FE   |
                    +----------------------+

External traffic:

FE
  -> custom login UI
  -> identity-service /api/v1/auth/login
     - nhận email/password
     - gọi Keycloak token endpoint
     - trả access token + refresh token cho FE
  -> api-gateway
     - verify user JWT via Keycloak issuer/JWK
     - route tới business services

Internal traffic:

Service A
  -> Keycloak token endpoint bằng client_credentials
  -> internal DNS
  -> Service B /internal/**
     - verify service JWT via Keycloak issuer/JWK
     - check caller/audience/scope/client role
```

## 3. Quyết Định Kiến Trúc

- Keycloak là nơi xác thực user và phát hành token.
- `identity-service` không tự cấp JWT sau refactor.
- `identity-service` giữ vai trò auth facade và user profile service.
- FE vẫn dùng login page riêng của MediBook.
- FE giữ `access_token` và `refresh_token`; app backend không giữ session.
- Refresh token lifecycle do Keycloak quản lý.
- Gateway verify access token bằng Keycloak issuer/JWK.
- Service-to-service không đi qua gateway; đi qua Docker/K8s internal DNS.
- Service-to-service dùng `client_credentials` token từ Keycloak.
- Scope/client role trong Keycloak là API-level permission giữa services.
- Business permission chi tiết không đặt trong Keycloak.

## 4. Phiên Bản Keycloak

Sử dụng image chính thức và pin version:

```text
quay.io/keycloak/keycloak:26.0.7
```

Không dùng:

```text
quay.io/keycloak/keycloak:latest
```

Lý do:

- Tránh breaking change ngoài kiểm soát.
- Dễ rollback.
- Dễ tái tạo môi trường local/CI/prod.

## 5. Thành Phần Cần Thêm

### 5.1 PostgreSQL Riêng Cho Keycloak

Service đề xuất trong `docker-compose.yml`:

```text
postgres-keycloak
```

Thông tin local:

```text
database: medibook_keycloak_db
user: keycloak
password: keycloak
container port: 5432
host port: 5442
volume: pg_keycloak_data
```

### 5.2 Keycloak Service

Service đề xuất:

```text
keycloak
```

Thông tin local:

```text
image: quay.io/keycloak/keycloak:26.0.7
command: start-dev --import-realm
container port: 8080
host port: 8180
admin username: admin
admin password: admin
realm: medibook
```

### 5.3 Realm Import

File đề xuất:

```text
keycloak/realm/medibook-realm.json
```

Realm import cần có:

- Realm `medibook`.
- Client `identity-service` cho auth facade.
- Client `medibook-web` nếu FE cần client riêng.
- Client cho các services.
- Roles nghiệp vụ cơ bản: `PATIENT`, `RECEPTIONIST`, `DOCTOR`, `ADMIN`.
- Client roles/scopes cho service-to-service.
- Protocol mapper để đưa `publicId` vào access token.
- Protocol mapper để đưa roles thành claim `roles` nếu muốn giữ compatibility với code hiện tại.

## 6. Flow Chính

### 6.1 User Login Qua UI Riêng

Mục tiêu: dùng login page riêng của MediBook nhưng Keycloak vẫn là nơi xác thực và cấp token.

```text
1. User nhập email/password trên UI MediBook.
2. FE gọi POST /api/v1/auth/login.
3. identity-service nhận LoginRequest.
4. identity-service gọi Keycloak token endpoint.
5. Keycloak xác thực user.
6. Keycloak trả access_token + refresh_token.
7. identity-service đồng bộ/resolve user nội bộ.
8. identity-service trả AuthResponse cho FE.
9. FE giữ token và gọi gateway bằng Authorization: Bearer <access_token>.
```

Keycloak token request:

```text
grant_type=password
client_id=identity-service
client_secret=<secret>
username=<email>
password=<password>
```

### 6.2 Refresh Token

Mục tiêu: refresh token do Keycloak quản lý, backend app không lưu session.

```text
1. FE gọi POST /api/v1/auth/refresh với refreshToken.
2. identity-service gọi Keycloak token endpoint.
3. Keycloak kiểm tra refresh token.
4. Keycloak trả access token mới và refresh token mới nếu rotation bật.
5. identity-service trả token mới cho FE.
```

Keycloak refresh request:

```text
grant_type=refresh_token
client_id=identity-service
client_secret=<secret>
refresh_token=<refresh_token>
```

### 6.3 Logout

Mục tiêu: revoke phiên/token ở Keycloak.

```text
1. FE gọi POST /api/v1/auth/logout với refreshToken.
2. identity-service gọi Keycloak logout endpoint.
3. Keycloak revoke refresh token/session.
4. identity-service trả success.
5. FE xóa token local.
```

### 6.4 Gateway Verify Token

Mục tiêu: gateway verify JWT từ Keycloak.

```text
1. FE gọi API qua gateway với Authorization: Bearer <access_token>.
2. gateway verify token bằng issuer-uri của Keycloak.
3. gateway route tới service đích.
```

Gateway config target:

```yaml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: ${KEYCLOAK_ISSUER_URI:http://localhost:8180/realms/medibook}
```

### 6.5 Service-To-Service Qua Client Credentials

Mục tiêu: service gọi service qua internal DNS, không đi qua gateway.

```text
1. Service A cần gọi Service B.
2. Service A xin token từ Keycloak bằng client_credentials.
3. Keycloak trả service access token.
4. Service A gọi Service B /internal/** qua internal DNS.
5. Service B verify token bằng Keycloak issuer/JWK.
6. Service B check caller/audience/scope/client role.
```

Ví dụ:

```text
workflow-service -> notification-service
scope/client role: notification:send
```

## 7. Đồng Bộ User Nội Bộ

MediBook hiện có bảng:

```text
users
roles
user_roles
refresh_tokens
```

Sau refactor:

- `users`, `roles`, `user_roles` tiếp tục dùng làm profile/role nội bộ.
- `refresh_tokens` không còn là nguồn refresh token chính.
- Password thuộc Keycloak.
- Cần thêm bảng mapping Keycloak user sang user nội bộ.

Bảng mới đề xuất:

```text
external_identities
- id
- provider
- external_subject
- user_id
- username
- email
- status
- created_at
- updated_at
```

Ràng buộc đề xuất:

```text
unique(provider, external_subject)
unique(provider, user_id)
foreign key user_id -> users(id)
```

Mapping:

```text
provider = KEYCLOAK
external_subject = Keycloak sub
user_id = users.id
```

Khi register:

```text
1. Tạo user trong Keycloak.
2. Tạo user profile trong users.
3. Set Keycloak user attribute publicId = users.public_id.
4. Tạo external_identities.
5. Assign role PATIENT nội bộ.
6. Assign role PATIENT trong Keycloak nếu token cần claim roles.
```

## 8. Claims Cần Trong Access Token

Để giảm thay đổi ở `patient-service`, access token nên có các claims tương thích với code hiện tại:

```json
{
  "sub": "keycloak-user-id",
  "preferred_username": "user@example.com",
  "email": "user@example.com",
  "publicId": "users.public_id",
  "roles": ["PATIENT"]
}
```

Hiện `patient-service` đang đọc:

```text
publicId
email
roles
```

Vì vậy nên cấu hình Keycloak protocol mappers:

- User attribute `publicId` -> claim `publicId`.
- Realm/client roles -> claim `roles`.

## 9. Refactor Theo Module

### 9.1 docker-compose.yml

Việc cần làm:

- Thêm `postgres-keycloak`.
- Thêm `keycloak`.
- Thêm volume `pg_keycloak_data`.
- Thêm env Keycloak cho `api-gateway`, `identity-service`, `patient-service`.
- Bỏ mount `identity-service/keys` nếu identity không tự ký JWT nữa.

### 9.2 api-gateway

Việc cần làm:

- Đổi resource server từ JWK nội bộ sang Keycloak issuer.
- Bỏ allowlist JWK cũ của identity-service.
- Giữ public auth endpoints:
  - `/api/v1/auth/register`
  - `/api/v1/auth/login`
  - `/api/v1/auth/refresh`
  - `/api/v1/auth/logout`
- Route `/api/v1/auth/**` về `identity-service`.

### 9.3 identity-service

Việc cần làm:

- Thêm `KeycloakProperties`.
- Thêm `KeycloakAuthClient` gọi token/logout endpoint.
- Thêm `KeycloakAdminClient` hoặc service tương đương để tạo user Keycloak.
- Refactor `AuthServiceImpl`:
  - `/login` gọi Keycloak.
  - `/refresh` gọi Keycloak.
  - `/logout` gọi Keycloak.
  - `/register` tạo user Keycloak + user nội bộ.
- Thêm `ExternalIdentity` entity/repository.
- Bỏ dependency runtime vào `JwtService`, `RefreshTokenService`.
- Bỏ `JwtAuthenticationFilter` khỏi `SecurityConfig`.
- Chuyển protected endpoints sang verify Keycloak JWT bằng Resource Server.

### 9.4 patient-service

Việc cần làm:

- Đổi issuer-uri sang Keycloak.
- Giữ `CurrentUserFacade` nếu token có `publicId`, `email`, `roles`.
- Nếu không cấu hình mapper `roles`, sửa converter để đọc `realm_access` hoặc `resource_access`.

### 9.5 Other Services

Việc cần làm khi service có protected API hoặc internal API:

- Thêm/giữ `spring-boot-starter-oauth2-resource-server` nếu cần verify JWT.
- Config `issuer-uri` về Keycloak.
- Với `/internal/**`, check service token scope/client role.
- Không gọi internal API qua gateway.

## 10. Checklist Triển Khai Theo Thứ Tự

### A. Chuẩn Bị Hạ Tầng Keycloak

- [x] Chọn và pin version Keycloak, mặc định `quay.io/keycloak/keycloak:26.0.7`.
- [x] Thêm `postgres-keycloak` vào `docker-compose.yml`.
- [x] Thêm volume `pg_keycloak_data`.
- [x] Thêm `keycloak` service vào `docker-compose.yml`.
- [x] Expose Keycloak local port `8180:8080`.
- [x] Cấu hình Keycloak dùng PostgreSQL riêng `medibook_keycloak_db`.
- [x] Chạy thử `docker compose up -d postgres-keycloak keycloak`.
- [x] Mở được Keycloak admin tại `http://localhost:8180`.

### B. Tạo Realm Và Clients

- [x] Tạo thư mục `keycloak/realm`.
- [x] Tạo realm import file `medibook-realm.json`.
- [x] Tạo realm `medibook`.
- [x] Tạo client `identity-service` dạng confidential.
- [x] Bật Direct Access Grants cho client `identity-service`.
- [x] Tạo client `medibook-web` nếu FE cần client riêng.
- [x] Tạo clients cho services.
- [x] Tạo roles nghiệp vụ `PATIENT`, `RECEPTIONIST`, `DOCTOR`, `ADMIN`.
- [x] Tạo client roles/scopes cho service-to-service.
- [x] Thêm protocol mapper claim `publicId`.
- [x] Thêm protocol mapper claim `roles`.
- [x] Import realm tự động khi compose up.

### C. Cập Nhật Cấu Hình Docker/Env

- [x] Thêm `KEYCLOAK_ISSUER_URI` cho `api-gateway`.
- [x] Thêm `KEYCLOAK_ISSUER_URI` cho `identity-service`.
- [x] Thêm `KEYCLOAK_TOKEN_URI` cho `identity-service`.
- [x] Thêm `KEYCLOAK_LOGOUT_URI` cho `identity-service`.
- [x] Thêm `KEYCLOAK_ADMIN_BASE_URL` cho `identity-service`.
- [x] Thêm `KEYCLOAK_REALM=medibook` cho `identity-service`.
- [x] Thêm `KEYCLOAK_CLIENT_ID=identity-service` cho `identity-service`.
- [x] Thêm `KEYCLOAK_CLIENT_SECRET` cho `identity-service`.
- [x] Thêm `KEYCLOAK_ISSUER_URI` cho `patient-service`.
- [ ] Bỏ env `JWT_*` khỏi `identity-service` nếu không còn dùng.
- [ ] Bỏ volume `./identity-service/keys` nếu identity không tự ký JWT nữa.

### D. Refactor Gateway

- [x] Đổi `api-gateway/src/main/resources/application.yaml` từ `jwk-set-uri` nội bộ sang `issuer-uri` Keycloak.
- [x] Cập nhật `application-local.yaml` nếu có cấu hình riêng.
- [x] Bỏ public matcher `/api/v1/auth/.well-known/jwks.json`.
- [x] Giữ public matcher cho login/register/refresh/logout.
- [ ] Verify gateway reject request không token.
- [ ] Verify gateway accept token Keycloak hợp lệ.

### E. Migration Identity DB

- [x] Tạo migration `V4__add_external_identities_for_keycloak.sql`.
- [x] Tạo bảng `external_identities`.
- [x] Tạo unique index `(provider, external_subject)`.
- [x] Tạo unique index `(provider, user_id)`.
- [x] Tạo FK `external_identities.user_id -> users.id`.
- [x] Cho phép `users.password_hash` nullable nếu password chuyển hẳn sang Keycloak.
- [x] Đánh dấu `refresh_tokens` là legacy hoặc dừng sử dụng trong code.

### F. Refactor Identity-Service Auth Facade

- [x] Thêm `KeycloakProperties`.
- [x] Thêm DTO mapping response Keycloak token.
- [x] Thêm `KeycloakAuthClient`.
- [x] Implement login bằng Keycloak password grant.
- [x] Implement refresh bằng Keycloak refresh token grant.
- [x] Implement logout bằng Keycloak logout endpoint.
- [x] Thêm `ExternalIdentity` entity.
- [x] Thêm `ExternalIdentityRepository`.
- [x] Implement register tạo user trong Keycloak.
- [x] Implement register tạo user profile trong DB nội bộ.
- [x] Set Keycloak user attribute `publicId`.
- [x] Tạo external identity mapping sau khi register.
- [x] Assign role nội bộ `PATIENT`.
- [x] Assign role Keycloak `PATIENT` nếu token cần claim roles.
- [x] Giữ response format `AuthResponse/JwtTokenResponse` để không phá FE.

### G. Bỏ JWT Tự Triển Khai Trong Identity-Service

- [ ] Bỏ dùng `JwtService` trong `AuthServiceImpl`.
- [ ] Bỏ dùng `RefreshTokenService` trong auth flow.
- [ ] Bỏ `JwtAuthenticationFilter` khỏi `SecurityConfig`.
- [ ] Bỏ route/controller JWK nội bộ nếu không còn dùng.
- [ ] Xóa dependency `jjwt-api` khỏi `identity-service/pom.xml`.
- [ ] Xóa dependency `jjwt-impl` khỏi `identity-service/pom.xml`.
- [ ] Xóa dependency `jjwt-jackson` khỏi `identity-service/pom.xml`.
- [ ] Bỏ config `security.jwt.*` khỏi application files.
- [ ] Build lại identity-service.

### H. Refactor Identity-Service Security Context

- [ ] Chuyển `identity-service` thành OAuth2 Resource Server nếu `/api/v1/users/**` cần protected.
- [ ] Sửa `CurrentUserFacade` để đọc principal dạng `Jwt`.
- [ ] Đọc `publicId` từ claim `publicId`.
- [ ] Đọc `email` từ claim `email`.
- [ ] Đọc roles từ claim `roles` hoặc Keycloak role structure.
- [ ] Verify `/api/v1/users/me` chạy với token Keycloak.

### I. Refactor Patient-Service

- [ ] Đổi issuer-uri sang Keycloak.
- [ ] Kiểm tra token có claim `publicId`.
- [ ] Kiểm tra token có claim `roles`.
- [ ] Nếu không có claim `roles`, sửa converter đọc Keycloak `realm_access/resource_access`.
- [ ] Verify `CurrentUserFacade.getCurrentUserPublicId()` hoạt động.
- [ ] Verify protected patient endpoint hoạt động với token Keycloak.

### J. Chuẩn Bị Service-To-Service

- [ ] Chốt convention `/internal/**`.
- [ ] Tạo client roles/scopes cho target services trong Keycloak.
- [ ] Cấu hình service clients dạng confidential.
- [ ] Bật service account cho service clients cần gọi nội bộ.
- [ ] Cấp role/scope phù hợp cho service accounts.
- [ ] Tạo helper lấy client_credentials token nếu bắt đầu implement internal calls.
- [ ] Cache service token theo audience/scope đến gần hết hạn.
- [ ] Service receiver verify issuer-uri Keycloak.
- [ ] Service receiver check client/audience/scope/client role cho `/internal/**`.

### K. Hardening Login UI Riêng

- [ ] Không log password.
- [ ] Không log access token.
- [ ] Không log refresh token.
- [ ] Bật brute force detection trong Keycloak realm.
- [ ] Bật refresh token rotation nếu phù hợp.
- [ ] Đặt access token TTL ngắn.
- [ ] Đặt refresh token TTL hợp lý.
- [ ] Rate limit `/api/v1/auth/login` nếu có gateway/filter hỗ trợ.
- [ ] FE không đưa token vào URL/query string.
- [ ] FE hạn chế lưu token ở `localStorage` nếu có thể.

### L. Verification

- [ ] `docker compose up -d postgres-keycloak keycloak` thành công.
- [ ] Keycloak admin mở được ở `http://localhost:8180`.
- [ ] Realm `medibook` tồn tại.
- [ ] Client `identity-service` tồn tại.
- [ ] `docker compose up --build -d` thành công.
- [ ] `POST /api/v1/auth/register` tạo user Keycloak + user DB + external identity.
- [ ] `POST /api/v1/auth/login` trả token Keycloak.
- [ ] Access token có issuer Keycloak realm `medibook`.
- [ ] Access token có claim `publicId`.
- [ ] Access token có claim `roles`.
- [ ] Gateway reject request không token.
- [ ] Gateway accept request có token Keycloak hợp lệ.
- [ ] `GET /api/v1/users/me` hoạt động.
- [ ] Protected patient API hoạt động.
- [ ] `POST /api/v1/auth/refresh` trả token mới.
- [ ] `POST /api/v1/auth/logout` revoke refresh token.
- [ ] Build toàn bộ: `mvn -q -DskipTests package`.

## 11. Rủi Ro Và Lưu Ý

- Direct password grant cần bật Direct Access Grants trong Keycloak client. Đây là trade-off để giữ UI login riêng và stateless.
- FE giữ refresh token nên cần hardening XSS và token storage.
- Nếu roles nằm cả DB nội bộ và Keycloak, cần quy tắc đồng bộ rõ ràng.
- Production không nên dùng `admin/admin` hoặc secret hard-code.
- Production nên dùng HTTPS và hostname Keycloak ổn định.
- Keycloak import realm local tiện cho dev, nhưng production nên quản lý realm config bằng quy trình riêng.
- Boot version trong MediBook chưa đồng nhất giữa gateway và services, cần build toàn bộ sau refactor.

## 12. Kết Luận

Phương án này chuyển MediBook sang Keycloak theo mô hình:

```text
FE dùng login UI riêng.
identity-service làm auth facade stateless.
Keycloak xác thực và cấp token.
FE giữ token.
gateway verify token qua Keycloak JWK.
identity-service giữ user profile và external identity mapping.
services verify Keycloak token khi cần.
service-to-service dùng client_credentials qua internal DNS.
```

Thiết kế này loại bỏ phần tự viết RSA/JWK/refresh token trong `identity-service`, tận dụng Keycloak cho OAuth2/OIDC chuẩn, nhưng vẫn giữ được API login riêng và mô hình user nội bộ của MediBook.
