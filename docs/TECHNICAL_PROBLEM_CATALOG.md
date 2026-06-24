# TECHNICAL PROBLEM CATALOG - MediBook

## 1. Muc Tieu
Tai lieu nay mo ta cac bai toan ky thuat cot loi cua MediBook. Moi bai toan ky thuat phai tra loi duoc cau hoi: no giai quyet rui ro nghiep vu nao va duoc ap dung o use case nao.

## TP-01: JWT RS256 Va RBAC Trong Microservice
Nghiep vu lien quan:
- Nguoi dung dang nhap va truy cap API theo role.
- Gateway va downstream service can xac thuc request.

Use case lien quan:
- UC-01
- UC-02
- UC-16

Van de ky thuat:
- Neu dung shared secret HMAC, moi service co secret deu co the ky token gia neu bi lo.
- Trong microservice can tach quyen issue token va verify token.

Huong giai quyet:
- identity-service giu private key va ky access token bang RS256.
- api-gateway va downstream verify token bang public key/JWKS.
- Refresh token la opaque token, hash truoc khi luu DB.
- JWT claims gom publicId, email, roles, iss, exp.

Rui ro can test:
- Token het han bi tu choi.
- Token sai chu ky bi tu choi.
- User thieu role bi 403.
- Refresh token cu khong dung lai duoc sau rotation.

## TP-02: Chong Double-Booking Slot
Nghiep vu lien quan:
- Hai benh nhan khong duoc dat cung mot slot cua cung bac si.

Use case lien quan:
- UC-08
- UC-09

Van de ky thuat:
- Nhieu request dong thoi co the cung thay slot AVAILABLE.
- Neu chi check trong code roi update sau, co the tao duplicate appointment.

Huong giai quyet:
- Doctor Schedule dung optimistic/pessimistic locking khi reserve slot.
- Slot co version va status.
- Appointment DB co unique partial index tren slot_id voi appointment active.
- Loi conflict tra 409 SLOT_NOT_AVAILABLE.

Rui ro can test:
- 2 request dong thoi reserve cung slot.
- 2 request dong thoi tao appointment cung slot.
- Service crash giua reserve va create appointment.

## TP-03: Slot Reservation Va TTL
Nghiep vu lien quan:
- Khi benh nhan bat dau thanh toan, slot can duoc giu tam thoi.
- Neu payment fail hoac timeout, slot phai mo lai.

Use case lien quan:
- UC-08
- UC-09
- UC-10
- UC-11

Van de ky thuat:
- Slot HELD co the bi treo neu payment khong callback.
- Neu release qua som co the lam mat lich hop le.

Huong giai quyet:
- Slot status: AVAILABLE, HELD, BOOKED, CLOSED.
- HELD co hold_expires_at.
- Scheduler release slot HELD qua han.
- Appointment PENDING_PAYMENT co payment deadline rieng.
- Payment success den sau timeout phai duoc xu ly theo state hien tai.

Rui ro can test:
- Payment khong callback.
- Payment success den sau khi appointment timeout.
- Release lap nhieu lan.

## TP-04: Idempotency Cho API Quan Trong
Nghiep vu lien quan:
- Client co the retry khi mang loi.
- User co the bam dat lich nhieu lan.
- Payment provider co the gui callback nhieu lan.

Use case lien quan:
- UC-09
- UC-10
- UC-11
- UC-13

Van de ky thuat:
- Retry co the tao duplicate appointment/payment.
- Cung mot Idempotency-Key voi payload khac la loi nghiem trong.

Huong giai quyet:
- Dung header Idempotency-Key cho command quan trong.
- Luu request hash va response snapshot.
- Cung key + cung payload tra lai response cu.
- Cung key + payload khac tra 409 IDEMPOTENCY_KEY_CONFLICT.
- Callback payment dung provider_txn_ref/event_id de dedupe.

Rui ro can test:
- Retry create appointment cung key.
- Retry cung key nhung doi slot.
- Callback success lap.
- Callback failed sau success.

## TP-05: Appointment State Machine
Nghiep vu lien quan:
- Appointment co vong doi ro rang, khong duoc chuyen trang thai tuy tien.

Use case lien quan:
- UC-09
- UC-11
- UC-12
- UC-13
- UC-18

Van de ky thuat:
- Neu controller/service set status tuy y, de tao trang thai vo nghia.
- Event den tre co the chuyen nguoc trang thai.

Huong giai quyet:
- Dinh nghia transition hop le:
  - PENDING_PAYMENT -> BOOKED
  - PENDING_PAYMENT -> CANCELLED
  - BOOKED -> CONFIRMED
  - BOOKED -> CANCELLED
  - CONFIRMED -> CANCELLED
  - CONFIRMED -> NO_SHOW
  - CONFIRMED -> CHECKED_IN o Phase 2
- Terminal state Phase 1: CANCELLED, NO_SHOW.
- Moi transition ghi appointment_status_history.
- Consumer event phai check current state truoc khi update.

Rui ro can test:
- Huy appointment da CANCELLED.
- Payment success cho appointment da timeout CANCELLED.
- Confirm lap BOOKED -> CONFIRMED.

## TP-06: Saga Compensation Booking-Payment
Nghiep vu lien quan:
- Dat lich co nhieu buoc qua schedule, appointment va payment.
- Khong co distributed transaction giua cac service.

Use case lien quan:
- UC-09
- UC-10
- UC-11

Van de ky thuat:
- Reserve slot thanh cong nhung create appointment loi.
- Appointment PENDING_PAYMENT nhung payment fail.
- Payment success nhung book slot loi.

Huong giai quyet:
- appointment-service lam orchestrator nhe.
- Buoc chinh: reserve slot -> create appointment -> init payment.
- Compensation: release slot, cancel appointment.
- Payment result xu ly qua event/API idempotent.
- Timeout job huy appointment PENDING_PAYMENT qua han.

Rui ro can test:
- Loi sau khi reserve slot.
- Payment fail.
- Payment success den lap.
- Payment success den sau cancel.

## TP-07: Transactional Outbox
Nghiep vu lien quan:
- Khi appointment booked/cancelled/rescheduled, notification va reporting can nhan event.

Use case lien quan:
- UC-14
- UC-15

Van de ky thuat:
- Neu publish RabbitMQ sau DB commit va app crash truoc publish, event bi mat.
- Neu publish truoc DB commit, consumer co the thay event cho du lieu chua commit.

Huong giai quyet:
- Ghi outbox_events cung transaction voi appointment.
- Background publisher doc event NEW va publish.
- Publish thanh cong mark PUBLISHED.
- Publish loi retry va tang attempt_count.
- Event co event_id duy nhat de consumer dedupe.

Rui ro can test:
- App crash sau commit appointment.
- RabbitMQ tam thoi down.
- Publisher chay lai va gap event da publish.

## TP-08: Event-Driven Notification Va Retry
Nghiep vu lien quan:
- Benh nhan can nhan xac nhan, doi/huy va nhac lich.

Use case lien quan:
- UC-14
- UC-15

Van de ky thuat:
- Provider gui SMS/email co the loi tam thoi.
- Event co the deliver lap.
- Khong duoc gui trung notification cho cung event.

Huong giai quyet:
- notification_messages co event_id hoac dedupe_key.
- notification_attempts luu moi lan gui.
- Retry toi da 3 lan voi backoff.
- Mock provider trong Phase 1 de test luong.
- Trang thai message: PENDING, SENT, FAILED.

Rui ro can test:
- Provider fail 2 lan roi success.
- Provider fail qua max retry.
- Event appointment.booked deliver lap.

## TP-09: Resilience Cho Service-To-Service Call
Nghiep vu lien quan:
- Appointment phai goi Doctor Schedule de reserve/release/book slot.
- Payment co the goi appointment/event consumer xu ly ket qua.

Use case lien quan:
- UC-08
- UC-09
- UC-10
- UC-11
- UC-12
- UC-13

Van de ky thuat:
- Service noi bo cham hoac loi co the lam treo request.
- Retry bua bai voi command khong idempotent co the tao duplicate.

Huong giai quyet:
- Timeout ngan cho internal call.
- Retry chi khi operation idempotent hoac co idempotency key.
- Circuit breaker cho call schedule/payment neu can.
- Loi service unavailable map ve error ro rang.

Rui ro can test:
- schedule-service timeout khi reserve.
- release slot retry lap.
- payment-service down khi init payment.

## TP-10: Observability Va Correlation ID
Nghiep vu lien quan:
- Khi dat lich loi, can biet request loi o service nao.

Use case lien quan:
- UC-16
- UC-17

Van de ky thuat:
- Request qua nhieu service va event nen log roi rac.
- Neu log PII se vi pham bao mat du lieu benh nhan.

Huong giai quyet:
- Gateway tao hoac forward X-Request-Id.
- Cac service log request id, user publicId, appointmentId neu co.
- Event payload chua correlationId.
- Error response co traceId/requestId.
- Khong log token, password, phone/email full neu khong can.

Rui ro can test:
- Request thieu X-Request-Id.
- Internal call khong forward request id.
- Event async mat correlation id.

## TP-11: Relationship-Based Authorization
Nghiep vu lien quan:
- User co the quan ly ho so nguoi than nhung khong duoc xem ho so nguoi khac.

Use case lien quan:
- UC-03
- UC-04

Van de ky thuat:
- Role PATIENT la chua du de quyet dinh user co duoc xem mot patient profile hay khong.
- Can permission theo quan he voi ho so.

Huong giai quyet:
- patient_relationships luu related_user_public_id, relationship_type, permission_level.
- OWNER/MANAGER duoc update.
- VIEWER chi duoc xem.
- Admin/Receptionist co API rieng va role check rieng.

Rui ro can test:
- User A doc patient cua User B.
- VIEWER update patient.
- Mot patient co hai primary contact.

## TP-12: API Gateway Boundary
Nghiep vu lien quan:
- Client chi goi public API qua gateway.
- Internal endpoint khong duoc expose ra internet/client.

Use case lien quan:
- UC-16

Van de ky thuat:
- Route sai prefix co the expose endpoint noi bo.
- Public/protected endpoint khong ro co the lam leak du lieu.

Huong giai quyet:
- Chuan hoa prefix /api/v1.
- Whitelist public endpoint it nhat co the.
- Internal endpoint dung /internal/v1 va khong route public.
- Gateway verify JWT truoc khi forward protected API.

Rui ro can test:
- Goi /internal/v1/slots/{id}/reserve qua gateway phai bi chan.
- Goi protected API khong token tra 401.
- Goi API role admin bang PATIENT tra 403.
