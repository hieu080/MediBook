# API EVENT CONTRACT - MediBook

## 1. Muc Tieu
Tai lieu nay chot cac contract nen thong nhat truoc khi nhieu nguoi trien khai song song. Day khong phai OpenAPI day du, ma la baseline ve URI, claims, status, event va error code.

## 2. API Convention
- Public/client API version: `/api/v1`.
- Internal service-to-service API version: `/internal/v1`.
- JSON UTF-8.
- UUID dung cho public identifier giua service.
- Command quan trong nen ho tro Idempotency-Key.
- Protected API yeu cau Authorization: Bearer <access_token>.
- Moi request nen co X-Request-Id. Neu client khong gui, gateway tao moi.

## 2.1 API Prefix Decision
Quyet dinh chinh thuc cho MediBook:

| Loai API | Prefix | Ai duoc goi | Gateway expose | Vi du |
| --- | --- | --- | --- | --- |
| Public/client API | `/api/v1` | Frontend, mobile, portal noi bo | Co | `/api/v1/auth/login`, `/api/v1/patients/me`, `/api/v1/appointments` |
| Internal API | `/internal/v1` | Service noi bo goi nhau | Khong | `/internal/v1/slots/{slotId}/reserve` |
| Actuator/health | `/actuator` | Infra/devops | Chi expose endpoint can thiet | `/actuator/health` |

Quy tac bat buoc:
- Tat ca API cho frontend phai bat dau bang `/api/v1`.
- Tat ca API service-to-service khong danh cho frontend phai bat dau bang `/internal/v1`.
- `api-gateway` khong route public cho `/internal/v1/**`.
- Khong dung cac prefix cu: `/api/patient/**`, `/api/schedule/**`, `/api/appointment/**`, `/api/payment/**`, `/api/notification/**`.
- Neu them API moi, phai cap nhat tai lieu nay va `docs/PROJECT_PROGRESS_CHECKLIST.md` neu anh huong tien do.

Quy tac versioning:
- Phase 1 chi expose `/api/v1` qua gateway.
- Khong cau hinh wildcard version nhu `/api/v*` de tranh expose version chua duoc thiet ke/test.
- Khi co `/api/v2`, phai them route explicit cho `/api/v2/**` va cap nhat contract/checklist.
- Version moi khong duoc pha vo contract cua version cu neu version cu van duoc support.
- `/api/v1` va `/api/v2` co the route cung service hoac khac service tuy muc do breaking change.
- Frontend chi duoc goi version da duoc document trong tai lieu nay.

Mapping prefix theo service Phase 1:

| Service | Public/client prefixes | Internal prefixes |
| --- | --- | --- |
| identity-service | `/api/v1/auth/**`, `/api/v1/users/**` | Chua can |
| patient-service | `/api/v1/patients/**` | Chua can |
| doctor-schedule-service | `/api/v1/facilities/**`, `/api/v1/specialties/**`, `/api/v1/doctors/**`, `/api/v1/schedules/**` | `/internal/v1/slots/**` |
| appointment-service | `/api/v1/appointments/**`, `/api/v1/reception/**` | `/internal/v1/appointments/**` neu dung REST internal |
| payment-service | `/api/v1/payments/**` | Chua can |
| notification-service | `/api/v1/notifications/**` neu can admin/read API | `/internal/v1/notifications/**` |

## 3. Common Response
Thanh cong:

```json
{
  "code": "SUCCESS",
  "message": "Request processed successfully",
  "data": {},
  "meta": {
    "requestId": "uuid-or-string"
  }
}
```

Loi:

```json
{
  "code": "VALIDATION_ERROR",
  "message": "Validation failed",
  "traceId": "uuid-or-string",
  "details": []
}
```

## 4. JWT Claims Contract
Access token nen co claims toi thieu:

```json
{
  "sub": "user@example.com",
  "iss": "identity-service",
  "iat": 1710000000,
  "exp": 1710000900,
  "publicId": "0fcd4d43-8b1b-4dd7-8f2d-347d54f2f7e3",
  "email": "user@example.com",
  "roles": ["PATIENT"],
  "fullName": "Nguyen Van A"
}
```

Quy tac:
- `publicId` la identity users.public_id.
- `roles` la danh sach role code.
- `fullName` la optional, khong dung cho authorization.
- Khong dua password hash, refresh token, du lieu y te vao JWT.

## 5. Role Contract
Role Phase 1:
- PATIENT
- DOCTOR
- RECEPTIONIST
- ADMIN

Role co the them sau:
- BILLING

## 6. Status Contract
### 6.1 Appointment Status
Phase 1 active:
- PENDING_PAYMENT
- BOOKED
- CONFIRMED
- CANCELLED
- NO_SHOW

Phase 2:
- CHECKED_IN
- IN_PROGRESS
- COMPLETED

Transition Phase 1:
- PENDING_PAYMENT -> BOOKED
- PENDING_PAYMENT -> CANCELLED
- BOOKED -> CONFIRMED
- BOOKED -> CANCELLED
- BOOKED -> NO_SHOW
- CONFIRMED -> CANCELLED
- CONFIRMED -> NO_SHOW

### 6.2 Slot Status
- AVAILABLE
- HELD
- BOOKED
- CLOSED

Transition:
- AVAILABLE -> HELD
- HELD -> AVAILABLE
- HELD -> BOOKED
- AVAILABLE -> CLOSED
- HELD -> CLOSED

### 6.3 Payment Status
- INIT
- SUCCESS
- FAILED
- REFUNDED

Transition:
- INIT -> SUCCESS
- INIT -> FAILED
- SUCCESS -> REFUNDED

### 6.4 Notification Status
- PENDING
- SENT
- FAILED

## 7. Error Code Baseline
Common:
- VALIDATION_ERROR
- BAD_REQUEST
- UNAUTHORIZED
- FORBIDDEN
- RESOURCE_NOT_FOUND
- INTERNAL_ERROR

Idempotency:
- IDEMPOTENCY_KEY_REQUIRED
- IDEMPOTENCY_KEY_CONFLICT

Identity:
- INVALID_CREDENTIALS
- USER_NOT_FOUND
- USER_INACTIVE
- USER_SUSPENDED
- ROLE_NOT_ASSIGNED

Patient:
- PATIENT_NOT_FOUND
- PATIENT_ALREADY_EXISTS
- PATIENT_ACCESS_DENIED
- PATIENT_RELATIONSHIP_NOT_FOUND
- PRIMARY_CONTACT_ALREADY_EXISTS

Schedule:
- DOCTOR_NOT_FOUND
- SPECIALTY_NOT_FOUND
- FACILITY_NOT_FOUND
- SCHEDULE_NOT_FOUND
- SCHEDULE_OVERLAPPED
- SLOT_NOT_FOUND
- SLOT_NOT_AVAILABLE
- SLOT_HOLD_EXPIRED

Appointment:
- APPOINTMENT_NOT_FOUND
- INVALID_APPOINTMENT_STATE
- INVALID_STATE_TRANSITION
- BOOKING_DEADLINE_EXCEEDED
- CANCEL_DEADLINE_EXCEEDED
- RESCHEDULE_DEADLINE_EXCEEDED

Payment:
- PAYMENT_NOT_FOUND
- PAYMENT_ALREADY_PROCESSED
- PAYMENT_CALLBACK_INVALID
- PAYMENT_SIGNATURE_INVALID

Notification:
- NOTIFICATION_TEMPLATE_NOT_FOUND
- NOTIFICATION_SEND_FAILED

## 8. API Contract Theo Service
### 8.1 Identity Service
Public:
- POST /api/v1/auth/register
- POST /api/v1/auth/login
- POST /api/v1/auth/refresh
- POST /api/v1/auth/logout
- GET /api/v1/auth/.well-known/jwks.json

Protected:
- POST /api/v1/auth/logout-all
- PATCH /api/v1/auth/default-role
- GET /api/v1/users/me

Admin:
- GET /api/v1/users/admin/{publicId}
- GET /api/v1/users/admin/by-email?email=

### 8.2 Patient Service
Protected PATIENT:
- POST /api/v1/patients/me
- GET /api/v1/patients/me
- PUT /api/v1/patients/me
- POST /api/v1/patients/dependents
- GET /api/v1/patients/dependents
- GET /api/v1/patients/{patientPublicId}
- PUT /api/v1/patients/{patientPublicId}

Admin/Receptionist optional:
- GET /api/v1/patients/search?keyword=
- GET /api/v1/patients/admin/{patientPublicId}

### 8.3 Doctor Schedule Service
Public read:
- GET /api/v1/facilities
- GET /api/v1/specialties
- GET /api/v1/doctors
- GET /api/v1/schedules/slots?date=&doctorId=&specialtyId=&facilityId=

Admin/Receptionist write:
- POST /api/v1/facilities
- POST /api/v1/specialties
- POST /api/v1/doctors
- PUT /api/v1/doctors/{doctorId}
- POST /api/v1/schedules
- POST /api/v1/schedules/{scheduleId}/generate-slots

Internal:
- POST /internal/v1/slots/{slotId}/reserve
- POST /internal/v1/slots/{slotId}/book
- POST /internal/v1/slots/{slotId}/release

### 8.4 Appointment Service
Protected:
- POST /api/v1/appointments
- GET /api/v1/appointments/{appointmentId}
- GET /api/v1/appointments/my
- PATCH /api/v1/appointments/{appointmentId}/reschedule
- PATCH /api/v1/appointments/{appointmentId}/cancel

Receptionist:
- GET /api/v1/reception/appointments?date=&doctorId=&status=
- PATCH /api/v1/appointments/{appointmentId}/confirm

Internal/Event handler optional:
- POST /internal/v1/appointments/{appointmentId}/payment-succeeded
- POST /internal/v1/appointments/{appointmentId}/payment-failed

### 8.5 Payment Service
Protected:
- POST /api/v1/payments/init
- GET /api/v1/payments/{paymentId}

Provider callback:
- POST /api/v1/payments/callback

### 8.6 Notification Service
Internal/admin:
- POST /internal/v1/notifications/send
- GET /api/v1/notifications/messages/{messageId}
- POST /api/v1/notifications/templates

## 9. Internal Slot API Payload
Reserve slot request:

```json
{
  "appointmentId": "uuid",
  "patientId": "uuid",
  "holdDurationSeconds": 600,
  "idempotencyKey": "string"
}
```

Reserve slot response:

```json
{
  "slotId": "uuid",
  "status": "HELD",
  "holdExpiresAt": "2026-06-24T10:15:00Z"
}
```

Book slot request:

```json
{
  "appointmentId": "uuid",
  "idempotencyKey": "string"
}
```

Release slot request:

```json
{
  "appointmentId": "uuid",
  "reason": "PAYMENT_FAILED",
  "idempotencyKey": "string"
}
```

## 10. Event Envelope
Moi event nen dung envelope chung:

```json
{
  "eventId": "uuid",
  "eventType": "appointment.booked",
  "eventVersion": 1,
  "occurredAt": "2026-06-24T10:00:00Z",
  "correlationId": "request-id",
  "source": "appointment-service",
  "aggregateType": "appointment",
  "aggregateId": "uuid",
  "payload": {}
}
```

Quy tac:
- `eventId` dung de dedupe consumer.
- `correlationId` lay tu X-Request-Id.
- `aggregateId` la public id cua entity chinh.
- Payload khong chua PII nhay cam qua muc can thiet.

## 11. Appointment Events
### 11.1 appointment.booked
```json
{
  "appointmentId": "uuid",
  "bookingCode": "A12345",
  "patientId": "uuid",
  "doctorId": "uuid",
  "facilityId": "uuid",
  "slotId": "uuid",
  "appointmentTime": "2026-06-24T10:00:00Z"
}
```

### 11.2 appointment.rescheduled
```json
{
  "appointmentId": "uuid",
  "oldSlotId": "uuid",
  "newSlotId": "uuid",
  "oldAppointmentTime": "2026-06-24T10:00:00Z",
  "newAppointmentTime": "2026-06-25T10:00:00Z",
  "reason": "Patient requested"
}
```

### 11.3 appointment.cancelled
```json
{
  "appointmentId": "uuid",
  "slotId": "uuid",
  "patientId": "uuid",
  "reason": "Patient requested",
  "cancelledBy": "uuid"
}
```

## 12. Payment Events
### 12.1 payment.succeeded
```json
{
  "paymentId": "uuid",
  "appointmentId": "uuid",
  "amount": 100000,
  "currency": "VND",
  "providerTxnRef": "provider-ref"
}
```

### 12.2 payment.failed
```json
{
  "paymentId": "uuid",
  "appointmentId": "uuid",
  "reason": "PAYMENT_DECLINED",
  "providerTxnRef": "provider-ref"
}
```

## 13. Notification Dedupe Key
Notification service nen tao dedupe key theo cong thuc:

```text
eventType + ':' + eventId + ':' + channel
```

Vi du:

```text
appointment.booked:4db0e66e-78df-4c20-80af-02e333034c6f:EMAIL
```

## 14. Idempotency Convention
Header:

```text
Idempotency-Key: client-generated-unique-key
```

Quy tac:
- Bat buoc voi POST /api/v1/appointments neu endpoint tao lich co payment hoac de phong retry.
- Bat buoc voi POST /api/v1/payments/init.
- Payment callback co the khong dung header nhung phai dedupe bang providerTxnRef hoac event id.
- Key nen co scope theo user + endpoint.

## 15. Correlation ID Convention
Header:

```text
X-Request-Id: request-id
```

Quy tac:
- Gateway tao neu client khong gui.
- Service forward header khi call noi bo.
- Event envelope copy vao correlationId.
- Error response tra traceId/requestId.
