# PROJECT PROGRESS CHECKLIST - MediBook

## 1. Muc Tieu
Tai lieu nay la checklist tien do toan du an. Muc dich la theo doi:

- Use case nao da hoan thien, dang lam, chua lam.
- API nao da co controller, service logic, security, migration, test.
- Service nao moi scaffold.
- Ghi chu cac phan chua hoan thien de chia viec tiep.

Quy uoc trang thai:

- DONE: da co API/logic chinh va co the dung trong flow hien tai.
- PARTIAL: da co mot phan, nhung chua du acceptance criteria.
- TODO: chua trien khai.
- BLOCKED: bi chan boi contract, dependency hoac quyet dinh chua chot.
- N/A: khong ap dung.

Ngay cap nhat gan nhat: 2026-06-24.

## 2. Tong Quan Trang Thai Service
| Service | Trang thai | Da co | Chua hoan thien / Note |
| --- | --- | --- | --- |
| api-gateway | PARTIAL | Gateway app, Spring Security resource server, route identity, JWKS config; route service Phase 1 da doi theo public prefix /api/v1; contract da chot internal prefix /internal/v1 | Chua chan /internal/v1 ro rang; chua co rate limit; chua co correlation-id filter rieng gateway; cac service downstream chua co API tuong ung |
| identity-service | PARTIAL | Auth API, User API, JWKS API, JWT RS256, refresh token, role/default role, Flyway V1-V3 | Can bo sung test nghiep vu; logout-all/default-role dang dung POST thay vi PATCH nhu contract; /users/me hien yeu cau PATIENT nen user role khac co the bi chan; chua co admin API gan role |
| patient-service | PARTIAL | Entity, DTO, mapper, exception, security, migration V1 cho patients va patient_relationships | Chua co controller; repository dang trong; service interface/impl dang trong; PatientServiceImpl chua implements PatientService; typo PatientRelatioshipRepository; chua co CRUD/authorization relationship |
| doctor-schedule-service | TODO | Application scaffold, config local DB | Chua co migration, entity, repository, service, controller, slot generation, reserve/book/release |
| appointment-service | TODO | Application scaffold, config local DB | Chua co migration, entity, repository, service, controller, idempotency, state machine, status history |
| payment-service | TODO | Application scaffold, config local DB | Chua co migration, payment init/callback, idempotency, event |
| notification-service | TODO | Application scaffold, config local DB | Chua co migration, template/message/attempt, consumer, retry |
| queue-service | TODO | Scaffold Phase 2 | Ngoai scope Phase 1 theo docs moi |
| reporting-service | TODO | Scaffold Phase 2 | Ngoai scope Phase 1 theo docs moi |
| audit-service | TODO | Scaffold Phase 2 | Ngoai scope Phase 1 theo docs moi |
| share-kernel | PARTIAL | ApiResponse, Meta, exception base, request id utilities/filter | Can chot error response dung chung; can xem service nao da register RequestIdFilter |

## 3. Checklist Use Case
| UC | Ten use case | Owner | Trang thai | API da co | Migration/data da co | Test | Note chua hoan thien |
| --- | --- | --- | --- | --- | --- | --- | --- |
| UC-01 | Dang Ky Va Dang Nhap | identity-service | PARTIAL | Co register/login/refresh/logout/logout-all | Co users/roles/user_roles/refresh_tokens | Chua thay unit/integration test nghiep vu | Can test refresh rotation, revoke, invalid credential; can review security cho logout-all |
| UC-02 | Quan Ly Role Va Default Role | identity-service | PARTIAL | Co POST /api/v1/auth/default-role; co GET /api/v1/users/me | Co user_roles.is_default va unique partial index | Chua thay test | Contract de xuat PATCH nhung code hien POST; chua co API admin gan role |
| UC-03 | Tao Ho So Benh Nhan Chinh | patient-service | PARTIAL | Chua co API | Co patients va patient_relationships | Chua co | Moi co DTO/entity/mapper; repository/service/controller chua trien khai |
| UC-04 | Quan Ly Ho So Nguoi Than | patient-service | PARTIAL | Chua co API | Co patient_relationships | Chua co | Moi co DTO/entity/mapper; chua co relationship authorization |
| UC-05 | Quan Ly Danh Muc Phong Kham | doctor-schedule-service | TODO | Chua co | Chua co | Chua co | Can tao facilities/specialties/doctors |
| UC-06 | Cau Hinh Lich Lam Viec Va Sinh Slot | doctor-schedule-service | TODO | Chua co | Chua co | Chua co | Can schedules/slots, overlap validation, generate slot idempotent |
| UC-07 | Tim Kiem Slot Kha Dung | doctor-schedule-service | TODO | Chua co | Chua co | Chua co | Can query filter va index |
| UC-08 | Reserve, Book Va Release Slot | doctor-schedule-service | TODO | Chua co | Chua co | Chua co | Can internal API, locking, TTL, scheduler release expired holds |
| UC-09 | Tao Lich Hen | appointment-service | TODO | Chua co | Chua co | Chua co | Can appointments, history, idempotency, state machine, call schedule reserve |
| UC-10 | Xu Ly Thanh Toan Giu Cho | payment-service | TODO | Chua co | Chua co | Chua co | Can payments, transactions, callback idempotency |
| UC-11 | Dong Bo Appointment Sau Payment | appointment-service | TODO | Chua co | Chua co | Chua co | Can consume payment event/API, compensation, timeout job |
| UC-12 | Doi Lich Hen | appointment-service | TODO | Chua co | Chua co | Chua co | Can policy deadline, reserve new slot before release old slot |
| UC-13 | Huy Lich Hen | appointment-service | TODO | Chua co | Chua co | Chua co | Can cancel idempotency, release slot, optional refund event |
| UC-14 | Notification Khi Lich Thay Doi | notification-service | TODO | Chua co | Chua co | Chua co | Can consume appointment event, retry, dedupe |
| UC-15 | Outbox Event Cho Appointment | appointment-service | TODO | Chua co | Chua co | Chua co | Can outbox_events va publisher |
| UC-16 | Gateway Routing Va Security Boundary | api-gateway | PARTIAL | Co route identity va route service khac theo /api/v1 | N/A | Chua co contract test | API prefix da chot: public /api/v1, internal /internal/v1. Route prefix da cap nhat; chua chan internal endpoints; downstream service chua co API tuong ung |
| UC-17 | Observability Cho Luong Booking | share-kernel/cross-service | PARTIAL | N/A | N/A | Chua co | Co RequestContextUtils/RequestIdFilter nhung chua xac minh tat ca service dung; chua co event correlationId |
| UC-18 | Le Tan Quan Ly Lich Trong Ngay | appointment-service | TODO | Chua co | Chua co | Chua co | Can receptionist query va confirm transition |

## 4. Checklist API Hien Co Trong Code
### 4.1 identity-service
| API | Controller | Trang thai | Security hien tai | Use case | Note |
| --- | --- | --- | --- | --- | --- |
| POST /api/v1/auth/register | AuthController | PARTIAL | Public qua gateway | UC-01 | Da co endpoint va service; can test validation, duplicate email, role default |
| POST /api/v1/auth/login | AuthController | PARTIAL | Public qua gateway | UC-01 | Da co endpoint va service; can test invalid credential/status |
| POST /api/v1/auth/refresh | AuthController | PARTIAL | Public qua gateway | UC-01 | Da co rotation revoke old token theo service; can test replay old refresh token |
| POST /api/v1/auth/logout | AuthController | PARTIAL | Public qua gateway | UC-01 | Logout bang refresh token; can xem co nen public hay protected tuy contract |
| POST /api/v1/auth/logout-all | AuthController | PARTIAL | Protected by any authenticated request o identity security neu cau hinh dung | UC-01 | Can xac minh gateway whitelist khong public endpoint nay; can test current user |
| POST /api/v1/auth/default-role | AuthController | PARTIAL | Protected by any authenticated request | UC-02 | Contract de xuat PATCH; can can nhac doi method khi chot API |
| GET /api/v1/auth/.well-known/jwks.json | JwksController | DONE | Public | UC-01/UC-16 | JWKS endpoint da co |
| GET /api/v1/users/public/{publicId} | UserController | PARTIAL | Dang nam sau gateway route /api/v1/users/** nen can token neu gateway bat anyExchange authenticated | UC-02 | Ten public nhung gateway co the dang protect; can chot co public hay internal |
| GET /api/v1/users/public/by-email | UserController | PARTIAL | Tuong tu tren | UC-02 | Can can nhac PII exposure neu public |
| GET /api/v1/users/me | UserController | PARTIAL | @PreAuthorize hasRole('PATIENT') | UC-02 | Nen cho authenticated user bat ky, khong chi PATIENT, neu multi-role/staff can xem me |
| GET /api/v1/users/admin/{publicId} | UserController | PARTIAL | @PreAuthorize hasRole('ADMIN') | UC-02 | Can test admin access |
| GET /api/v1/users/admin/by-email | UserController | PARTIAL | @PreAuthorize hasRole('ADMIN') | UC-02 | Can test admin access va PII logging |

### 4.2 api-gateway
| Route | Trang thai | Use case | Note |
| --- | --- | --- | --- |
| /api/v1/auth/** -> identity-service | PARTIAL | UC-16 | Hoat dong theo config; can dam bao public/protected matcher chinh xac |
| /api/v1/users/** -> identity-service | PARTIAL | UC-16 | Hoat dong theo config; all protected tru public matcher |
| /api/v1/patients/** -> patient-service | PARTIAL | UC-16 | Gateway route da dung prefix; patient-service chua co controller API |
| /api/v1/facilities/**, /api/v1/specialties/**, /api/v1/doctors/**, /api/v1/schedules/** -> doctor-schedule-service | PARTIAL | UC-16 | Gateway route da dung prefix; doctor-schedule-service chua co controller API |
| /api/v1/appointments/**, /api/v1/reception/** -> appointment-service | PARTIAL | UC-16 | Gateway route da dung prefix; appointment-service chua co controller API |
| /api/v1/payments/** -> payment-service | PARTIAL | UC-16 | Gateway route da dung prefix; payment-service chua co controller API |
| /api/v1/notifications/** -> notification-service | PARTIAL | UC-16 | Gateway route da dung prefix; notification-service chua co controller API |
| /api/v1/queue/** -> queue-service | N/A | Phase 2 | Route scaffold dung prefix nhung ngoai scope Phase 1 |
| /api/v1/reports/** -> reporting-service | N/A | Phase 2 | Route scaffold dung prefix nhung ngoai scope Phase 1 |
| /api/v1/audit/** -> audit-service | N/A | Phase 2 | Route scaffold dung prefix nhung ngoai scope Phase 1 |

### 4.3 patient-service
| API theo contract | Trang thai | Use case | Note |
| --- | --- | --- | --- |
| POST /api/v1/patients/me | TODO | UC-03 | Chua co controller/service/repository |
| GET /api/v1/patients/me | TODO | UC-03 | Chua co controller/service/repository |
| PUT /api/v1/patients/me | TODO | UC-03 | Chua co controller/service/repository |
| POST /api/v1/patients/dependents | TODO | UC-04 | Chua co controller/service/repository |
| GET /api/v1/patients/dependents | TODO | UC-04 | Chua co controller/service/repository |
| GET /api/v1/patients/{patientPublicId} | TODO | UC-04 | Chua co relationship authorization |
| PUT /api/v1/patients/{patientPublicId} | TODO | UC-04 | Chua co relationship authorization |

### 4.4 doctor-schedule-service
| API theo contract | Trang thai | Use case | Note |
| --- | --- | --- | --- |
| GET /api/v1/facilities | TODO | UC-05 | Chua co implementation |
| POST /api/v1/facilities | TODO | UC-05 | Chua co implementation |
| GET /api/v1/specialties | TODO | UC-05 | Chua co implementation |
| POST /api/v1/specialties | TODO | UC-05 | Chua co implementation |
| GET /api/v1/doctors | TODO | UC-05 | Chua co implementation |
| POST /api/v1/doctors | TODO | UC-05 | Chua co implementation |
| PUT /api/v1/doctors/{doctorId} | TODO | UC-05 | Chua co implementation |
| POST /api/v1/schedules | TODO | UC-06 | Chua co implementation |
| GET /api/v1/schedules | TODO | UC-06 | Chua co implementation |
| POST /api/v1/schedules/{scheduleId}/generate-slots | TODO | UC-06 | Chua co implementation |
| GET /api/v1/schedules/slots | TODO | UC-07 | Chua co implementation |
| POST /internal/v1/slots/{slotId}/reserve | TODO | UC-08 | Chua co implementation; khong expose public gateway |
| POST /internal/v1/slots/{slotId}/book | TODO | UC-08 | Chua co implementation; khong expose public gateway |
| POST /internal/v1/slots/{slotId}/release | TODO | UC-08 | Chua co implementation; khong expose public gateway |

### 4.5 appointment-service
| API theo contract | Trang thai | Use case | Note |
| --- | --- | --- | --- |
| POST /api/v1/appointments | TODO | UC-09 | Chua co implementation |
| GET /api/v1/appointments/{appointmentId} | TODO | UC-09 | Chua co implementation |
| GET /api/v1/appointments/my | TODO | UC-09 | Chua co implementation |
| PATCH /api/v1/appointments/{appointmentId}/reschedule | TODO | UC-12 | Chua co implementation |
| PATCH /api/v1/appointments/{appointmentId}/cancel | TODO | UC-13 | Chua co implementation |
| GET /api/v1/reception/appointments | TODO | UC-18 | Chua co implementation |
| PATCH /api/v1/appointments/{appointmentId}/confirm | TODO | UC-18 | Chua co implementation |
| POST /internal/v1/appointments/{appointmentId}/payment-succeeded | TODO | UC-11 | Optional neu khong dung broker |
| POST /internal/v1/appointments/{appointmentId}/payment-failed | TODO | UC-11 | Optional neu khong dung broker |

### 4.6 payment-service
| API theo contract | Trang thai | Use case | Note |
| --- | --- | --- | --- |
| POST /api/v1/payments/init | TODO | UC-10 | Chua co implementation |
| GET /api/v1/payments/{paymentId} | TODO | UC-10 | Chua co implementation |
| POST /api/v1/payments/callback | TODO | UC-10 | Chua co implementation |

### 4.7 notification-service
| API/event theo contract | Trang thai | Use case | Note |
| --- | --- | --- | --- |
| Consume appointment.booked | TODO | UC-14 | Chua co consumer |
| Consume appointment.rescheduled | TODO | UC-14 | Chua co consumer |
| Consume appointment.cancelled | TODO | UC-14 | Chua co consumer |
| Reminder scheduler T-24h/T-2h | TODO | UC-14 | Chua co scheduler |
| POST /internal/v1/notifications/send | TODO | UC-14 | Optional internal API, chua co implementation |
| GET /api/v1/notifications/messages/{messageId} | TODO | UC-14 | Chua co implementation |
| POST /api/v1/notifications/templates | TODO | UC-14 | Chua co implementation |

## 5. Checklist Database Migration
| Service | Migration hien co | Trang thai | Note |
| --- | --- | --- | --- |
| identity-service | V1__init.sql, V2__add_doctor_role.sql, V3__add_default_role_to_user_roles.sql | PARTIAL | Co users/roles/user_roles/refresh_tokens; chua co login_history/session nang cao |
| patient-service | V1__init.sql | PARTIAL | Co patients/patient_relationships; can xem lai column user_id vs entity publicUserId naming; can bo sung audit columns neu chot |
| doctor-schedule-service | Chua co | TODO | Can facilities/specialties/doctors/schedules/slots |
| appointment-service | Chua co | TODO | Can appointments/appointment_status_history/idempotency_records/outbox_events |
| payment-service | Chua co | TODO | Can payments/payment_transactions/refunds optional |
| notification-service | Chua co | TODO | Can notification_templates/messages/attempts |
| queue-service | Chua co | N/A Phase 2 | De sau |
| reporting-service | Chua co | N/A Phase 2 | De sau |
| audit-service | Chua co | N/A Phase 2 | De sau |

## 6. Checklist Technical Problems
| Technical problem | Trang thai | Da co | Chua hoan thien / Note |
| --- | --- | --- | --- |
| TP-01 JWT RS256 va RBAC | PARTIAL | identity-service co JwtService/JwtKeyProvider/JWKS; gateway verify JWKS | Can test E2E; downstream moi patient-service co config security, cac service khac chua co resource server config ro |
| TP-02 Chong Double-Booking Slot | TODO | Chua co | Can schedule slot locking + appointment unique active slot |
| TP-03 Slot Reservation va TTL | TODO | Chua co | Can slot status HELD, hold_expires_at, scheduler |
| TP-04 Idempotency | TODO | Common error code co idempotency | Chua co idempotency_records/logic |
| TP-05 Appointment State Machine | TODO | Chua co | Can enum, transition validator, history |
| TP-06 Saga Compensation | TODO | Chua co | Can appointment orchestrator |
| TP-07 Transactional Outbox | TODO | Chua co | Can outbox_events va publisher |
| TP-08 Event-Driven Notification Retry | TODO | Chua co | Can consumer/dedupe/retry |
| TP-09 Resilience Service-To-Service | TODO | Chua co | Can timeout/retry/circuit breaker cho appointment -> schedule/payment |
| TP-10 Observability/Correlation ID | PARTIAL | share-kernel co RequestContextUtils/RequestIdFilter | Can ap dung tat ca service, forward header/event correlationId |
| TP-11 Relationship-Based Authorization | TODO | patient schema co relationship | Chua co service logic authorization |
| TP-12 API Gateway Boundary | PARTIAL | gateway co security va routes | Route prefix chua chuan; internal endpoint policy chua co |

## 7. Viec Uu Tien Tiep Theo
### 7.1 De hoan thien UC-01/UC-02
- Them unit/integration test cho register/login/refresh/logout/default-role.
- Kiem tra gateway public whitelist: logout-all va default-role phai protected.
- Can nhac doi POST /api/v1/auth/default-role thanh PATCH theo contract hoac cap nhat contract.
- Dieu chinh GET /api/v1/users/me de authenticated user bat ky co the xem thong tin chinh minh, khong chi PATIENT.
- Bo sung API admin gan role neu can cho UC-02 day du.

### 7.2 De hoan thien UC-03/UC-04
- Doi PatientRelatioshipRepository thanh PatientRelationshipRepository.
- Cho PatientRepository extend JpaRepository.
- Cho PatientRelationshipRepository extend JpaRepository.
- Cho PatientServiceImpl implements PatientService.
- Them PatientController.
- Implement /api/v1/patients/me va /api/v1/patients/dependents.
- Implement relationship authorization OWNER/MANAGER/VIEWER.

### 7.3 De bat dau doctor-schedule-service
- Tao migration V1 cho facilities/specialties/doctors/schedules/slots.
- Tao enum status.
- Tao CRUD admin cho master data.
- Tao generate slot idempotent.
- Tao query slot available.
- Tao internal reserve/book/release voi locking.

### 7.4 De bat dau appointment-service
- Tao migration V1 cho appointments/status_history/idempotency_records/outbox_events.
- Tao state machine validator.
- Tao POST /api/v1/appointments voi Idempotency-Key.
- Tich hop internal reserve slot.
- Tao cancel/reschedule/confirm.

### 7.5 De bat dau payment/notification
- Payment: tao payments/payment_transactions va mock callback idempotent.
- Notification: tao templates/messages/attempts va mock provider retry.
- Chot RabbitMQ hay internal REST cho giai doan dau.

## 8. Definition Of Done Theo Use Case
Mot use case chi duoc danh dau DONE khi co du cac muc:

- Migration DB neu co persistence.
- Entity/repository/service/controller hoac consumer/scheduler tuong ung.
- Validation request.
- Authorization theo role/resource.
- Error mapping ro rang.
- Test toi thieu cho happy path va loi quan trong.
- README/API docs hoac contract duoc cap nhat neu API thay doi.
- Neu la command quan trong, co idempotency hoac ghi chu vi sao chua can.
- Neu phat event, co outbox/dedupe hoac ghi chu giai doan mock.

## 9. Luu Y Khi Cap Nhat Checklist
- Khi tao API moi, cap nhat muc 4.
- Khi hoan thien use case, cap nhat muc 3 va ghi ro evidence: controller, service, test, migration.
- Khi them migration, cap nhat muc 5.
- Khi giai quyet technical problem, cap nhat muc 6.
- Khong danh dau DONE chi vi da co scaffold.
