# JWT Public/Private Key Architecture For MediBook

## 1. Mục tiêu tài liệu
Tài liệu này mô tả hướng triển khai xác thực và ủy quyền dùng JWT theo mô hình public/private key cho hệ thống MediBook.

Mục tiêu là để team thống nhất:
- Vì sao nên dùng asymmetric signing thay cho shared secret.
- Vai trò của `identity-service`, `api-gateway` và các service downstream.
- Cách phát hành, xác minh và truyền ngữ cảnh người dùng giữa các service.
- Các quyết định kỹ thuật nên làm ngay, nên hoãn, và lộ trình rollout thực tế.

Tài liệu này ưu tiên phương án "chuẩn nhất nhưng vẫn có thể triển khai được" cho codebase hiện tại.

## 2. Bối cảnh hệ thống hiện tại
MediBook đang có các thành phần chính:
- `identity-service`: quản lý đăng ký, đăng nhập, refresh token, logout.
- `api-gateway`: entry point cho client.
- Các service nghiệp vụ khác như `patient-service`, `appointment-service`, `payment-service`, `queue-service`, `audit-service`...
- `share-kernel`: chứa response, request id filter, exception chung.

Hiện tại hệ thống đã có:
- JWT access token.
- Refresh token lưu DB.
- API gateway làm entry point.
- `identity-service` là nơi cấp token.

Điểm còn cần thống nhất là mô hình xác thực chuẩn cho toàn hệ thống.

## 3. Vì sao nên dùng public/private key
### 3.1 Shared secret có hạn chế gì
Nếu dùng HMAC secret chung:
- `identity-service` dùng secret để ký token.
- `api-gateway` và các service khác cũng phải có cùng secret để verify.

Rủi ro:
- Service nào verify được thì cũng có thể ký giả token nếu lộ secret.
- Secret bị chia sẻ cho nhiều service, khó kiểm soát phạm vi ảnh hưởng khi lộ.
- Không tách được quyền “issue token” và quyền “verify token”.

### 3.2 Public/private key giải quyết ra sao
Nếu dùng asymmetric key, ví dụ `RS256`:
- `identity-service` giữ **private key** để ký token.
- `api-gateway` và các service khác giữ **public key** để verify token.

Lợi ích:
- Chỉ `identity-service` ký được token.
- Gateway/service downstream chỉ verify được, không thể tự ký token giả.
- Mô hình phù hợp hơn với microservice.
- Dễ mở rộng về sau sang JWKS hoặc key rotation.

## 4. Quyết định kiến trúc đề xuất
### 4.1 Quyết định chính
Đề xuất chọn:
- Access token: JWT ký bằng `RS256`.
- Refresh token: opaque token random, lưu DB trong `identity-service`.
- `identity-service`: phát hành access token và refresh token.
- `api-gateway`: verify access token bằng public key.
- Downstream service: cũng verify access token bằng public key cho các endpoint protected.

### 4.2 Vì sao refresh token không nên là JWT lúc này
Refresh token hiện nên giữ là opaque token vì:
- Dễ revoke.
- Dễ rotate.
- Dễ lưu DB và truy vết session.
- Không cần các service khác hiểu refresh token.

Với MediBook hiện tại:
- Access token dùng asymmetric JWT.
- Refresh token dùng DB-backed opaque token.

Đây là hướng rất thực tế và đúng chuẩn triển khai.

## 5. Kiến trúc luồng xác thực end-to-end
## 5.1 Đăng nhập
1. Client gọi `POST /api/v1/auth/login` tới `api-gateway`.
2. Gateway route sang `identity-service`.
3. `identity-service` xác thực email/password.
4. `identity-service` tạo:
   - access token JWT ký bằng private key.
   - refresh token random lưu DB.
5. Response trả về client gồm:
   - `accessToken`
   - `refreshToken`
   - `expiresIn`
   - `tokenType`
   - thông tin user cơ bản.

## 5.2 Gọi API protected
1. Client gửi `Authorization: Bearer <accessToken>` tới `api-gateway`.
2. `api-gateway` verify chữ ký token bằng public key.
3. Nếu hợp lệ, gateway forward request đến downstream service.
4. Downstream service tiếp tục verify access token bằng public key.
5. Nếu hợp lệ, downstream service lấy claims/current user và xử lý nghiệp vụ.

## 5.3 Refresh token
1. Client gọi `POST /api/v1/auth/refresh` với refresh token.
2. Gateway route sang `identity-service`.
3. `identity-service` validate refresh token trong DB.
4. `identity-service` revoke refresh token cũ.
5. `identity-service` cấp:
   - access token mới
   - refresh token mới
6. Trả response mới cho client.

## 5.4 Logout một phiên
1. Client gọi `POST /api/v1/auth/logout` với refresh token.
2. `identity-service` revoke refresh token đó.
3. Các access token cũ sống đến khi hết hạn tự nhiên.

## 5.5 Logout tất cả phiên
1. Client gọi `POST /api/v1/auth/logout-all` với access token hợp lệ.
2. `identity-service` lấy current user từ access token.
3. `identity-service` revoke toàn bộ refresh token active của user.
4. Các access token cũ vẫn sống tới khi hết hạn tự nhiên.

## 6. Phân vai theo service
## 6.1 Identity Service
Trách nhiệm:
- Xác thực user.
- Phát hành access token.
- Phát hành refresh token.
- Rotate refresh token.
- Revoke refresh token.
- Quản lý private key.

Identity service không nên làm:
- Tin cậy vào header nội bộ của service khác để xác thực user.
- Chia sẻ private key cho gateway/downstream service.

## 6.2 API Gateway
Trách nhiệm:
- Nhận request từ client.
- Verify access token bằng public key.
- Chặn request không hợp lệ trước khi vào downstream.
- Route request đến đúng service.

Gateway không nên:
- Có private key.
- Tự phát hành token.

## 6.3 Downstream Services
Trách nhiệm:
- Verify access token bằng public key cho protected endpoint.
- Lấy current user từ token/security context.
- Kiểm tra role/permission ở mức service nếu cần.

Downstream service không nên:
- Phụ thuộc hoàn toàn vào việc gateway đã verify rồi.
- Tự ký token.

## 7. Chuẩn hóa key và thuật toán
## 7.1 Thuật toán
Khuyến nghị dùng:
- `RS256`

Lý do:
- Phổ biến.
- Hỗ trợ tốt bởi thư viện Java/JJWT/Spring Security.
- Dễ giải thích cho team.
- Dễ chuyển sang JWKS về sau.

## 7.2 Key pair
Cần 1 cặp key:
- private key: dùng để sign access token.
- public key: dùng để verify access token.

Ví dụ local/dev:
- `private_key.pem`
- `public_key.pem`

## 7.3 Nơi lưu key
### Local/dev
Có thể dùng:
- file `.pem` mount vào container
- hoặc environment variable nếu team muốn đơn giản hơn

Khuyến nghị:
- local/dev dùng file `.pem` rõ ràng hơn để học và debug

### Prod
Khuyến nghị:
- secret manager / vault / cloud KMS
- tuyệt đối không commit private key vào repo

## 8. Cách phân phối public key
### 8.1 Giai đoạn đầu
Cách đơn giản nhất:
- cấu hình cùng một public key ở `api-gateway` và các downstream service.

Ưu điểm:
- Dễ làm.
- Dễ rollout.

Nhược điểm:
- Khi rotate key phải cập nhật config ở nhiều service.

### 8.2 Giai đoạn chuẩn hơn
Dùng JWKS endpoint:
- `identity-service` public `/.well-known/jwks.json` hoặc endpoint tương đương.
- Gateway và service downstream tải public key từ đó để verify.

Ưu điểm:
- Chuẩn hơn.
- Dễ key rotation.
- Dễ scale hệ thống về sau.

Khuyến nghị cho MediBook:
- Phase đầu: dùng public key tĩnh qua config.
- Phase sau: nâng lên JWKS.

## 9. Nội dung access token nên chứa gì
### 9.1 Claims tối thiểu
Khuyến nghị access token có:
- `sub`: email hoặc public user identifier.
- `iss`: identity-service.
- `iat`
- `exp`
- `publicId`
- `roles`

### 9.2 Claims có thể thêm
Có thể thêm nếu thật sự cần:
- `fullName`
- `aud`

### 9.3 Claims không nên thêm
Không nên nhét vào token:
- password hash
- refresh token
- dữ liệu y tế
- thông tin PII nhạy cảm quá mức
- payload quá lớn

## 10. Xác thực ở gateway và service downstream
## 10.1 Gateway cần verify gì
Gateway nên kiểm tra:
- Token có tồn tại không.
- Header format có đúng `Bearer <token>` không.
- Chữ ký có hợp lệ không.
- Token có hết hạn không.
- `iss` có đúng không.
- Nếu dùng `aud`, phải check `aud` luôn.

## 10.2 Downstream service cần verify gì
Khuyến nghị downstream service cũng verify:
- Chữ ký.
- Expiration.
- Issuer.

Điều này giúp giảm việc “tin gateway tuyệt đối”.

## 10.3 Có cần truyền internal header không
Có thể truyền thêm internal header để tiện dùng, ví dụ:
- `X-Auth-User-Email`
- `X-Auth-User-PublicId`
- `X-Auth-User-Roles`

Nhưng các header này chỉ nên là **phụ trợ**, không phải nguồn tin cậy duy nhất.

Nguồn tin cậy chính vẫn nên là:
- token đã verify thành công
- hoặc security context downstream dựng từ token đó

## 11. Cấu trúc class đề xuất cho MediBook
## 11.1 Trong identity-service
Nên có:
- `JwtProperties`
- `JwtClaimsFactory`
- `JwtService`
- `RefreshTokenService`
- `AuthService`
- `AuthController`
- `CustomUserDetail`
- `CustomUserDetailsService`
- `JwtAuthenticationFilter`

Cần thay đổi:
- `JwtService` chuyển từ HMAC secret sang load private key.
- verify logic ở `identity-service` cũng dùng public key nếu cần parse signed token thống nhất.

### Đề xuất thêm class cho key
- `KeyProperties`
- `KeyLoader` hoặc `JwtKeyProvider`

Ví dụ trách nhiệm:
- `JwtKeyProvider`:
  - load private key từ file/env
  - load public key từ file/env
  - expose `PrivateKey` và `PublicKey`

## 11.2 Trong api-gateway
Nên có:
- `GatewayJwtProperties`
- `GatewayJwtVerifier`
- `AuthenticationGlobalFilter` hoặc `JwtAuthenticationGatewayFilter`
- config danh sách public endpoints

Gateway filter nên:
- bỏ qua public endpoint
- validate JWT
- nếu hợp lệ thì cho route tiếp
- nếu không hợp lệ thì trả `401/403`

## 11.3 Trong service downstream
Mỗi service protected nên có:
- `JwtVerifier`
- `JwtAuthenticationFilter`
- `CurrentUserFacade`
- `SecurityConfig`

Nếu team muốn giảm lặp code về sau, có thể tách phần verify JWT sang `share-kernel` hoặc module security riêng. Tuy nhiên chỉ nên làm sau khi flow đầu tiên đã chạy ổn.

## 12. Key rotation
## 12.1 Tại sao cần nghĩ trước
Sớm hay muộn key cũng cần rotate:
- vì bảo mật
- vì incident response
- vì policy của tổ chức

## 12.2 Mô hình tối thiểu
Trong token nên có `kid`.

Luồng:
- `identity-service` ký token bằng key hiện tại với `kid=current-key-id`
- verifier nhìn `kid` để chọn đúng public key

### Giai đoạn đầu
Nếu chưa dùng JWKS, vẫn nên thiết kế code có chỗ cho `kid`.

### Giai đoạn sau
JWKS sẽ giúp phục vụ nhiều public key đồng thời:
- key mới
- key cũ còn hiệu lực verify trong thời gian chuyển tiếp

## 13. Bảo mật thực tế cho refresh token
Dù chọn asymmetric JWT cho access token, refresh token vẫn nên:
- random opaque string
- hash trước khi lưu DB
- revoke được
- rotate khi refresh

Mức đang nên có cho MediBook:
- revoke old refresh token khi refresh
- issue refresh token mới
- `logout` revoke 1 token
- `logout-all` revoke toàn bộ token active của user

Có thể để phase sau:
- refresh token reuse detection
- device binding
- IP/device risk score
- session audit nâng cao

## 14. Quyết định triển khai được khuyến nghị cho team
### 14.1 Quyết định nên chốt ngay
1. Access token dùng `RS256`.
2. Refresh token vẫn là opaque token trong DB.
3. `identity-service` giữ private key.
4. `api-gateway` và downstream services giữ public key.
5. Gateway verify token.
6. Downstream services cũng verify token cho protected endpoint.
7. Public key giai đoạn đầu phân phối qua config/file.
8. Thiết kế code có chỗ cho `kid` từ đầu.

### 14.2 Quyết định có thể hoãn
1. JWKS endpoint.
2. Key rotation đầy đủ.
3. Device/IP risk engine.
4. Token revocation blacklist cho access token.
5. Permission engine phức tạp hơn role.

## 15. Lộ trình triển khai đề xuất
## Bước 1. Chuẩn hóa trong identity-service
- Đổi `JwtService` sang ký bằng private key.
- Tạo `JwtKeyProvider`.
- Chuẩn hóa claims, issuer.
- Giữ refresh token như hiện tại.

## Bước 2. Làm verifier ở api-gateway
- Tạo `GatewayJwtVerifier` dùng public key.
- Thêm filter xác thực toàn cục.
- Định nghĩa public endpoint whitelist.

## Bước 3. Làm verifier ở một service downstream đầu tiên
Khuyến nghị chọn service gần nhất để test end-to-end, ví dụ:
- `patient-service`
- hoặc `identity-service` chính các endpoint protected hiện tại

## Bước 4. Chuẩn hóa security module dùng chung nếu thật sự cần
Chỉ khi pattern đã ổn, cân nhắc tách phần verify chung ra module riêng.

## Bước 5. Bổ sung JWKS và key rotation
Sau khi flow cơ bản chạy ổn.

## 16. Những câu hỏi team cần thống nhất trong buổi thảo luận
1. Có chấp nhận rollout public key tĩnh trước khi làm JWKS không?
2. Downstream service có verify JWT ngay từ phase đầu không, hay chỉ gateway verify trước?
3. `sub` nên là email hay `publicId`?
4. Có dùng `aud` ngay từ đầu không?
5. Private/public key sẽ được cung cấp cho local/dev/prod bằng cách nào?
6. Có muốn tạo module verify JWT dùng chung sau khi xong bản đầu tiên không?

## 17. Khuyến nghị cuối cùng
Nếu mục tiêu là triển khai chuẩn và học đúng ngay từ đầu, khuyến nghị cuối cùng cho MediBook là:
- Dùng `RS256` cho access token.
- Giữ refresh token là opaque token trong DB.
- `identity-service` là token issuer duy nhất.
- `api-gateway` và downstream service đều verify access token bằng public key.
- Giai đoạn đầu dùng public key tĩnh qua config/file.
- Thiết kế code có chỗ cho `kid` và JWKS từ đầu.

Đây là phương án vừa đúng kiến trúc microservice, vừa đủ thực tế để team triển khai theo từng bước mà không phải đập đi làm lại quá nhiều về sau.
