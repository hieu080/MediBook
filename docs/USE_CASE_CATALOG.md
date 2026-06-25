# USE CASE CATALOG - MediBook

## 1. Muc Tieu
Tai lieu nay chia nghiep vu MediBook thanh cac use case doc lap, ro owner service, ro API/data boundary va ro bai toan ky thuat kho. Muc tieu la nhieu nguoi co the trien khai song song ma khong dam chan len nhau.

## 2. Nguyen Tac Chia Use Case
- Moi use case co mot service owner chinh.
- Use case chi so huu du lieu trong bounded context cua no.
- Giao tiep giua service qua API/event contract, khong truy cap DB cheo.
- Moi use case co acceptance criteria doc lap.
- Bai toan ky thuat phai gan voi rui ro nghiep vu thuc te.

## UC-01: Dang Ky Va Dang Nhap
Actor: Patient, Staff, Admin

Service owner: identity-service

Muc tieu nghiep vu:
- Nguoi dung co tai khoan hop le de su dung he thong.
- He thong cap access token va refresh token de truy cap API protected.

API chinh:
- POST /api/v1/auth/register
- POST /api/v1/auth/login
- POST /api/v1/auth/refresh
- POST /api/v1/auth/logout
- POST /api/v1/auth/logout-all

Du lieu chinh:
- users
- roles
- user_roles
- refresh_tokens

Khong thuoc use case nay:
- Khong tao ho so benh nhan.
- Khong quan ly appointment.
- Khong xu ly authorization tren patient relationship.

Bai toan ky thuat kho:
- JWT RS256 voi private key o identity-service va public key/JWKS cho gateway/downstream.
- Refresh token opaque luu hash trong DB.
- Rotation refresh token khi refresh.
- Revoke token khi logout.
- Khong log password, token, refresh token raw.

Acceptance criteria:
- Dang ky user thanh cong voi role mac dinh PATIENT.
- Dang nhap tra accessToken, refreshToken, expiresIn va user info.
- Refresh token cu bi revoke sau khi refresh thanh cong.
- Logout revoke refresh token.
- Token hop le goi duoc API protected qua gateway.

## UC-02: Quan Ly Role Va Default Role
Actor: User co nhieu role, Admin

Service owner: identity-service

Muc tieu nghiep vu:
- Mot user co the co nhieu role, nhung frontend can biet role mac dinh de hien thi UI phu hop.

API chinh:
- GET /api/v1/users/me
- PATCH /api/v1/auth/default-role
- GET /api/v1/users/admin/{publicId}

Du lieu chinh:
- roles
- user_roles
- user_roles.is_default

Khong thuoc use case nay:
- Khong quan ly permission theo resource.
- Khong gan relationship tren patient.

Bai toan ky thuat kho:
- Dam bao moi user chi co mot default role active.
- Partial unique index cho default role.
- Khong cho user doi sang role chua duoc gan.
- JWT claims can ro roles de downstream authorization.

Acceptance criteria:
- User co nhieu role doi duoc default role.
- Doi default role khong lam mat role khac.
- User doi sang role chua duoc gan nhan loi 403/422.
- DB khong cho hai default role active cho cung mot user.

## UC-03: Tao Ho So Benh Nhan Chinh
Actor: Patient

Service owner: patient-service

Muc tieu nghiep vu:
- Patient tao va quan ly ho so kham cua chinh minh sau khi dang ky.

API chinh:
- POST /api/v1/patients/me
- GET /api/v1/patients/me
- PUT /api/v1/patients/me

Du lieu chinh:
- patients
- patient_relationships

Khong thuoc use case nay:
- Khong tao account.
- Khong tao ho so nguoi than.
- Khong dat lich.

Bai toan ky thuat kho:
- Lien ket logic patients.user_id voi identity users.public_id ma khong FK cheo DB.
- Moi user chi co mot ho so chinh.
- Authorization dua tren JWT publicId.
- Tra ve dung du lieu rieng tu, khong lo PII cua benh nhan khac.

Acceptance criteria:
- Patient tao duoc ho so chinh.
- Patient khong tao duoc ho so chinh thu hai.
- Patient chi xem/sua duoc ho so cua minh.
- Khi tao ho so chinh, he thong tao relationship SELF/OWNER.

## UC-04: Quan Ly Ho So Nguoi Than
Actor: Patient, Guardian

Service owner: patient-service

Muc tieu nghiep vu:
- User tao va quan ly ho so cho con, bo me, vo/chong hoac nguoi phu thuoc.

API chinh:
- POST /api/v1/patients/dependents
- GET /api/v1/patients/dependents
- GET /api/v1/patients/{patientPublicId}
- PUT /api/v1/patients/{patientPublicId}

Du lieu chinh:
- patients
- patient_relationships

Khong thuoc use case nay:
- Khong xu ly claim/link ho so khi nguoi phu thuoc tu tao account.
- Khong dat lich.

Bai toan ky thuat kho:
- Relationship-based authorization.
- Phan quyen OWNER, MANAGER, VIEWER.
- Mot ho so co nhieu nguoi lien quan nhung chi mot primary contact.
- Khong cho user tu gan quyen vao ho so khong thuoc minh.

Acceptance criteria:
- User tao duoc ho so nguoi than.
- User tao ho so nguoi than duoc gan relationship PARENT/GUARDIAN/CAREGIVER voi permission MANAGER hoac OWNER.
- User co OWNER/MANAGER duoc sua ho so.
- User khong co relationship bi tu choi 403.

## UC-05: Quan Ly Danh Muc Phong Kham
Actor: Admin

Service owner: doctor-schedule-service

Muc tieu nghiep vu:
- Admin cau hinh co so, chuyen khoa va bac si de benh nhan co du lieu tim lich kham.

API chinh:
- POST /api/v1/facilities
- GET /api/v1/facilities
- POST /api/v1/specialties
- GET /api/v1/specialties
- POST /api/v1/doctors
- GET /api/v1/doctors
- PUT /api/v1/doctors/{doctorId}

Du lieu chinh:
- facilities
- specialties
- doctors

Khong thuoc use case nay:
- Khong tao slot.
- Khong dat lich.
- Khong quan ly appointment.

Bai toan ky thuat kho:
- Chuan hoa master data tranh trung chuyen khoa/bac si.
- Soft delete/inactive thay vi xoa cung.
- Filter bac si theo co so, chuyen khoa, trang thai.
- Dung UUID public identifier, khong expose internal id neu co.

Acceptance criteria:
- Admin tao duoc co so, chuyen khoa, bac si.
- Patient xem duoc danh sach bac si active.
- Khong tao trung specialty_code.
- Bac si inactive khong xuat hien trong API public.

## UC-06: Cau Hinh Lich Lam Viec Va Sinh Slot
Actor: Admin, Receptionist

Service owner: doctor-schedule-service

Muc tieu nghiep vu:
- Phong kham cau hinh ngay gio lam viec cua bac si va sinh slot theo thoi luong kham.

API chinh:
- POST /api/v1/schedules
- GET /api/v1/schedules
- POST /api/v1/schedules/{scheduleId}/generate-slots

Du lieu chinh:
- schedules
- slots

Khong thuoc use case nay:
- Khong reserve slot.
- Khong tao appointment.

Bai toan ky thuat kho:
- Sinh slot khong trung khi chay lai.
- Kiem tra end_time > start_time.
- Khong cho lich overlap cho cung bac si.
- Unique constraint doctor_id + slot_start.
- Xu ly timezone Asia/Ho_Chi_Minh o API nhung luu UTC.

Acceptance criteria:
- Admin tao lich lam viec thanh cong.
- He thong sinh slot theo slot_duration_min.
- Generate lai khong tao duplicate.
- Lich overlap bi tu choi.

## UC-07: Tim Kiem Slot Kha Dung
Actor: Patient, Receptionist

Service owner: doctor-schedule-service

Muc tieu nghiep vu:
- Nguoi dung tim khung gio con trong theo ngay, bac si, chuyen khoa va co so.

API chinh:
- GET /api/v1/schedules/slots?date=&doctorId=&specialtyId=&facilityId=

Du lieu chinh:
- slots
- doctors
- specialties
- facilities

Khong thuoc use case nay:
- Khong reserve slot.
- Khong tao appointment.

Bai toan ky thuat kho:
- Query toi uu theo filter.
- Chi tra slot AVAILABLE.
- Khong tra slot qua gan gio kham neu policy khong cho dat.
- Co the cache slot hot bang Redis o giai doan sau.

Acceptance criteria:
- Tra dung slot available.
- Filter theo ngay, chuyen khoa, bac si, co so hoat dong.
- Slot HELD, BOOKED, CLOSED khong xuat hien.
- P95 doc duoi muc tieu, vi du 500ms voi dataset test.

## UC-08: Reserve, Book Va Release Slot
Actor: appointment-service

Service owner: doctor-schedule-service

Muc tieu nghiep vu:
- Slot duoc giu tam thoi khi bat dau dat lich, duoc book khi thanh cong va release khi that bai/timeout.

API noi bo:
- POST /internal/v1/slots/{slotId}/reserve
- POST /internal/v1/slots/{slotId}/book
- POST /internal/v1/slots/{slotId}/release

Du lieu chinh:
- slots.status
- slots.hold_expires_at
- slots.version

Khong thuoc use case nay:
- Khong tao appointment.
- Khong xu ly payment.
- Khong gui notification.

Bai toan ky thuat kho:
- Race condition khi nhieu request reserve cung slot.
- Optimistic locking hoac pessimistic locking.
- TTL hold slot.
- Idempotency cho reserve/release.
- Scheduler release slot het han.
- Tra 409 khi slot khong con available.

Acceptance criteria:
- Hai request dong thoi reserve cung slot chi mot thanh cong.
- Slot HELD co hold_expires_at.
- Slot het han tu ve AVAILABLE.
- Slot BOOKED khong the release tuy tien.

## UC-09: Tao Lich Hen
Actor: Patient, Receptionist

Service owner: appointment-service

Muc tieu nghiep vu:
- Patient hoac le tan dat lich kham tren mot slot hop le.

API chinh:
- POST /api/v1/appointments

Du lieu chinh:
- appointments
- appointment_status_history
- idempotency_records neu trien khai rieng

Khong thuoc use case nay:
- Khong quan ly slot DB truc tiep.
- Khong verify payment callback.
- Khong gui notification truc tiep neu da dung event.

Bai toan ky thuat kho:
- Idempotency-Key cho create appointment.
- Chong double-booking bang schedule reserve va unique partial index appointment active.
- State ban dau PENDING_PAYMENT hoac BOOKED tuy flow.
- Saga orchestration nhe: reserve slot, create appointment, init payment.
- Compensation neu buoc sau loi.

Acceptance criteria:
- Tao appointment thanh cong khi slot available.
- Retry cung Idempotency-Key tra cung ket qua.
- Cung Idempotency-Key nhung payload khac tra 409.
- Hai request cung slot chi mot appointment active duoc tao.
- Co status history tu null sang PENDING_PAYMENT hoac BOOKED.

## UC-10: Xu Ly Thanh Toan Giu Cho
Actor: Patient, External payment provider hoac mock provider

Service owner: payment-service

Muc tieu nghiep vu:
- Ghi nhan giao dich dat coc/giu cho truoc khi xac nhan lich.

API chinh:
- POST /api/v1/payments/init
- POST /api/v1/payments/callback

Du lieu chinh:
- payments
- payment_transactions

Khong thuoc use case nay:
- Khong tao appointment.
- Khong book slot truc tiep.
- Khong gui notification truc tiep.

Bai toan ky thuat kho:
- Callback co the den tre, den lap hoac sai chu ky.
- Idempotent callback.
- Verify signature o muc mock hoac provider that.
- Payment status transition INIT -> SUCCESS/FAILED, SUCCESS -> REFUNDED.
- Publish event payment.succeeded hoac payment.failed.

Acceptance criteria:
- Init payment tao payment INIT.
- Callback success chuyen payment sang SUCCESS.
- Callback lap khong tao sai trang thai.
- Callback failed khong rollback payment da SUCCESS.
- Callback invalid signature bi tu choi hoac duoc luu transaction invalid theo thiet ke.

## UC-11: Dong Bo Appointment Sau Payment
Actor: Internal event/API

Service owner: appointment-service

Muc tieu nghiep vu:
- Payment thanh cong thi lich duoc xac nhan. Payment that bai hoac timeout thi lich bi huy va slot duoc giai phong.

Event/API:
- Consume payment.succeeded
- Consume payment.failed
- Scheduler scan appointment PENDING_PAYMENT timeout

Du lieu chinh:
- appointments
- appointment_status_history

Khong thuoc use case nay:
- Khong verify payment provider.
- Khong gui notification provider truc tiep.

Bai toan ky thuat kho:
- Saga compensation.
- Event idempotency.
- Payment success den sau timeout.
- Khong chuyen sai trang thai neu appointment da CANCELLED.
- At-least-once delivery ket hop idempotent consumer.

Acceptance criteria:
- payment.succeeded chuyen PENDING_PAYMENT sang BOOKED.
- payment.failed chuyen PENDING_PAYMENT sang CANCELLED va release slot.
- Timeout cung cancel va release slot.
- Event lap khong tao duplicate status history.

## UC-12: Doi Lich Hen
Actor: Patient, Receptionist

Service owner: appointment-service

Muc tieu nghiep vu:
- Doi appointment sang slot khac truoc deadline cho phep.

API chinh:
- PATCH /api/v1/appointments/{appointmentId}/reschedule

Du lieu chinh:
- appointments
- appointment_status_history

Khong thuoc use case nay:
- Khong thay doi lich lam viec bac si.
- Khong xu ly refund.

Bai toan ky thuat kho:
- Policy deadline doi lich.
- Reserve slot moi truoc khi release slot cu.
- Compensation neu reserve slot moi thanh cong nhung update appointment loi.
- Idempotent reschedule.
- Khong doi appointment o terminal state.

Acceptance criteria:
- Appointment BOOKED/CONFIRMED doi duoc neu con truoc deadline.
- Slot moi duoc book, slot cu duoc release.
- Neu slot moi bi chiem, appointment cu giu nguyen.
- Status history ghi ro thay doi.

## UC-13: Huy Lich Hen
Actor: Patient, Receptionist

Service owner: appointment-service

Muc tieu nghiep vu:
- Huy appointment theo policy cua phong kham.

API chinh:
- PATCH /api/v1/appointments/{appointmentId}/cancel

Du lieu chinh:
- appointments
- appointment_status_history

Khong thuoc use case nay:
- Khong truc tiep refund provider.
- Khong xoa slot.

Bai toan ky thuat kho:
- Cancel idempotency.
- Policy deadline huy.
- Terminal state validation.
- Release slot sau cancel.
- Neu appointment da thanh toan, phat event/request refund neu can.

Acceptance criteria:
- Appointment active huy duoc neu policy cho phep.
- Cancel lap tra ket qua on dinh.
- Slot duoc release neu lich chua hoan tat.
- Khong huy duoc appointment COMPLETED, NO_SHOW hoac terminal state khong hop le.

## UC-14: Notification Khi Lich Thay Doi
Actor: Patient, System

Service owner: notification-service

Muc tieu nghiep vu:
- Benh nhan nhan xac nhan dat lich, thong bao doi/huy va nhac lich truoc gio kham.

Event/API:
- Consume appointment.booked
- Consume appointment.rescheduled
- Consume appointment.cancelled
- Scheduler reminder T-24h/T-2h

Du lieu chinh:
- notification_templates
- notification_messages
- notification_attempts

Khong thuoc use case nay:
- Khong quyet dinh appointment status.
- Khong retry appointment/payment.
- Khong chua business rule dat lich.

Bai toan ky thuat kho:
- Event-driven consumer idempotency.
- Retry toi da 3 lan.
- Exponential backoff.
- Mock SMS/email provider.
- Khong gui trung message khi event deliver lap.
- Template rendering an toan.

Acceptance criteria:
- appointment.booked tao notification message.
- Gui thanh cong chuyen SENT.
- Gui fail retry toi da 3 lan.
- Event trung khong gui trung neu cung event id.
- Reminder duoc schedule dung thoi diem.

## UC-15: Outbox Event Cho Appointment
Actor: Internal system

Service owner: appointment-service

Muc tieu nghiep vu:
- Khi appointment thay doi trang thai, event phai duoc phat dang tin cay cho notification/payment/reporting sau nay.

Du lieu chinh:
- outbox_events

Khong thuoc use case nay:
- Khong xu ly notification provider.
- Khong xu ly payment provider.

Bai toan ky thuat kho:
- Transactional outbox.
- Ghi appointment va outbox event cung transaction.
- Background publisher retry.
- Event status NEW, PUBLISHED, FAILED.
- Tranh mat event khi app crash sau commit DB nhung truoc publish broker.

Acceptance criteria:
- Appointment booked/cancelled/rescheduled tao outbox event.
- Publisher gui event sang RabbitMQ hoac mock broker.
- Publish loi duoc retry.
- Khong publish duplicate neu event da PUBLISHED.

## UC-16: Gateway Routing Va Security Boundary
Actor: Client, API Gateway

Service owner: api-gateway

Muc tieu nghiep vu:
- Client truy cap he thong qua gateway, public/protected endpoint ro rang.

Public API:
- /api/v1/auth/register
- /api/v1/auth/login
- /api/v1/auth/refresh
- /api/v1/doctors
- /api/v1/schedules/slots

Protected API:
- /api/v1/patients/**
- /api/v1/appointments/**
- /api/v1/payments/**
- admin APIs

Khong thuoc use case nay:
- Khong chua business logic.
- Khong tu quyet dinh permission theo resource chi tiet.

Bai toan ky thuat kho:
- Route prefix chuan /api/v1.
- JWT resource server qua JWKS.
- Forward correlation id.
- Khong expose internal endpoints.
- Rate limit login/booking neu mo rong.

Acceptance criteria:
- Tat ca API Phase 1 di qua gateway.
- Public endpoint khong can token.
- Protected endpoint thieu/sai token tra 401.
- Internal endpoint khong expose public route.

## UC-17: Observability Cho Luong Booking
Actor: Developer, Operator

Service owner: share-kernel va cac service lien quan

Muc tieu nghiep vu:
- Khi dat lich loi, team truy vet duoc request qua nhieu service.

Pham vi:
- Request ID/correlation ID.
- Structured logging.
- Log business event quan trong.
- Actuator health/metrics.

Khong thuoc use case nay:
- Khong lam reporting nghiep vu.
- Khong thay the audit service.

Bai toan ky thuat kho:
- Truyen correlation id qua gateway, internal call va event.
- Log khong chua PII nhay cam.
- Dong bo format error response.
- Debug duoc flow phan tan.

Acceptance criteria:
- Moi request co X-Request-Id.
- Log cua gateway/appointment/schedule/payment co cung request id.
- Error response co trace/request id.
- Health endpoint hoat dong tren tung service.

## UC-18: Le Tan Quan Ly Lich Trong Ngay
Actor: Receptionist

Service owner: appointment-service

Muc tieu nghiep vu:
- Le tan xem lich kham trong ngay, xac nhan lich, ho tro doi/huy cho benh nhan.

API chinh:
- GET /api/v1/reception/appointments?date=&doctorId=&status=
- PATCH /api/v1/appointments/{appointmentId}/confirm

Du lieu chinh:
- appointments
- appointment_status_history

Khong thuoc use case nay:
- Khong check-in/queue.
- Khong kham benh.
- Khong thanh toan tai quay chi tiet.

Bai toan ky thuat kho:
- Role-based access cho RECEPTIONIST.
- Query appointment theo ngay, bac si, status hieu qua.
- Confirm transition hop le.
- Khong cho receptionist sua du lieu clinical hoac user role.

Acceptance criteria:
- Receptionist xem duoc lich theo ngay.
- Receptionist confirm duoc BOOKED -> CONFIRMED.
- Patient khong goi duoc receptionist API.
- Confirm lap khong tao loi trang thai.

## 3. Ma Tran Chia Viec
- Nhom Identity/Gateway: UC-01, UC-02, UC-16.
- Nhom Patient: UC-03, UC-04.
- Nhom Doctor Schedule: UC-05, UC-06, UC-07, UC-08.
- Nhom Appointment: UC-09, UC-11, UC-12, UC-13, UC-18.
- Nhom Payment: UC-10.
- Nhom Notification/Event: UC-14, UC-15.
- Nhom Cross-cutting: UC-17.

## 4. Dieu Kien Lam Song Song
Truoc khi code song song can chot:

- JWT claims contract.
- Common response/error contract.
- UUID public identifier format.
- Internal slot API contract.
- Payment event contract.
- Appointment event contract.
- Idempotency-Key convention.
- Correlation ID convention.
