# PROJECT BUSINESS SCOPE - MediBook

## 1. Muc Tieu Tai Lieu
Tai lieu nay dinh hinh lai MediBook theo mot bai toan nghiep vu cu the de tranh trien khai chung chung. Day la baseline cho backlog, use case, API contract va cac bai toan ky thuat can giai quyet trong du an hoc thuat.

## 2. Problem Statement
MediBook la he thong dat lich kham cho phong kham tu hoac phong kham dich vu nhieu bac si.

Phong kham hien dang tiep nhan lich qua dien thoai, Zalo, Excel hoac ghi chep thu cong. Cach van hanh nay dan den cac van de:

- Le tan mat nhieu thoi gian hoi lich trong, xac nhan va sua lich.
- Benh nhan khong tu xem duoc khung gio con trong.
- De dat trung slot khi nhieu nguoi thao tac cung luc.
- Benh nhan quen lich hoac khong den kham.
- Khi co thanh toan giu cho, slot co the bi treo neu payment fail hoac callback den tre.
- Trang thai lich hen thay doi qua nhieu buoc nhung khong co lich su ro rang.
- Khi loi xay ra, kho truy vet request qua gateway, appointment, schedule, payment va notification.

MediBook giai quyet bai toan:

> Xay dung mot luong dat lich kham dang tin cay trong kien truc microservice: benh nhan tu dat lich, phong kham quan ly bac si va slot, he thong chong dat trung, xu ly retry/callback loi, dong bo trang thai appointment-payment-notification va ghi nhan duoc lich su thay doi.

## 3. Doi Tuong Su Dung
- Patient: benh nhan hoac nguoi nha dat lich, quan ly ho so ca nhan va ho so nguoi than.
- Receptionist: le tan xem lich trong ngay, xac nhan, doi, huy lich ho tro benh nhan.
- Admin: quan tri co so, chuyen khoa, bac si, lich lam viec va phan quyen nhan su.
- Doctor: xem lich kham cua minh. Trong Phase 1 chi can doc lich, chua lam benh an.
- External Payment Provider: cong thanh toan hoac mock provider gui callback.
- Notification Provider: SMS/email/mock provider gui thong bao.

## 4. Gia Tri Nghiep Vu Phase 1
Phase 1 tap trung vao luong dat lich end-to-end:

1. Patient dang ky va dang nhap.
2. Patient tao ho so chinh hoac ho so nguoi than.
3. Admin cau hinh co so, chuyen khoa, bac si va lich lam viec.
4. He thong sinh slot kham tu lich lam viec.
5. Patient tim slot trong theo ngay, bac si, chuyen khoa, co so.
6. Patient dat lich tren slot con trong.
7. He thong reserve slot va chong dat trung.
8. Neu co thanh toan giu cho, payment-service xu ly init/callback.
9. Appointment chuyen trang thai theo state machine.
10. Notification gui xac nhan va nhac lich.
11. Receptionist xem lich trong ngay, xac nhan, doi hoac huy lich.

## 5. Pham Vi Phase 1
### 5.1 In Scope
- Dang ky, dang nhap, refresh token, logout.
- JWT RS256 va RBAC co ban.
- Quan ly role va default role.
- Quan ly ho so benh nhan chinh.
- Quan ly ho so nguoi than va relationship authorization.
- Quan ly co so, chuyen khoa, bac si.
- Cau hinh lich lam viec va sinh slot.
- Tim kiem slot kha dung.
- Reserve, book va release slot.
- Tao, doi, huy va xac nhan appointment.
- State machine va status history cho appointment.
- Payment mock/basic de hoc idempotent callback va saga compensation.
- Notification mock/basic de hoc event-driven, retry va idempotent consumer.
- Outbox event cho appointment.
- Gateway routing va security boundary.
- Request id, logging va health check co ban.

### 5.2 Out Of Scope Phase 1
- Queue/check-in tai quay.
- Benh an, don thuoc, chi dinh xet nghiem.
- Bao hiem y te nang cao.
- HIS/EMR integration.
- Reporting dashboard nang cao.
- Audit service tap trung day du.
- Provider payment/SMS that trong production.
- Telemedicine.

## 6. Pham Vi Phase 2
- Check-in va hang doi kham bang queue-service.
- Doctor encounter co ban neu can mo rong sang luong kham.
- Reporting KPI co ban: booking success, cancellation, no-show, slot utilization.
- Audit service tap trung cho thao tac nghiep vu quan trong.
- Payment/refund nang cao.

## 7. Pham Vi Phase 3
- Tich hop HIS/EMR.
- Bao hiem nang cao.
- Rule engine toi uu lich.
- Key rotation/JWKS nang cao.
- Notification multi-provider.
- Load test va scaling theo production-like setup.

## 8. Nguyen Tac Domain Boundary
- Identity chi quan ly tai khoan, token, role. Khong luu ho so benh nhan.
- Patient chi quan ly ho so benh nhan va relationship. Khong luu password/role.
- Doctor Schedule quan ly master data, schedule va slot. Khong tao appointment.
- Appointment quan ly lich hen, state machine, policy doi/huy. Khong tu sua truc tiep DB cua slot/payment.
- Payment quan ly giao dich va callback. Khong tu quyet dinh appointment status neu khong qua event/API contract.
- Notification chi gui thong bao theo event. Khong quyet dinh nghiep vu appointment/payment.
- Gateway chi route va bao ve boundary. Khong chua business logic.
- Share-kernel chi chua phan dung chung that su: response, error, request id, utility nho.

## 9. Chu De Ky Thuat Trung Tam
Chu de ky thuat cua MediBook nen la:

> Reliable Appointment Booking in Microservice Architecture.

Tu mot nghiep vu dat lich kham, du an can chung minh cach xu ly cac rui ro ky thuat thuc te:

- Authentication va authorization trong microservice.
- Race condition khi nhieu request dat cung mot slot.
- Idempotency khi client retry hoac callback den lap.
- State machine de khong chuyen trang thai sai.
- Saga compensation khi payment fail hoac timeout.
- Outbox pattern de khong mat event.
- Async notification co retry va idempotent consumer.
- Observability de truy vet request qua nhieu service.

## 10. Luong Chuan Khong Thanh Toan
1. Admin tao co so, chuyen khoa, bac si.
2. Admin tao lich lam viec va sinh slot.
3. Patient dang ky, dang nhap.
4. Patient tao ho so.
5. Patient tim slot trong.
6. Patient tao appointment.
7. Appointment reserve slot o doctor-schedule-service.
8. Appointment tao lich trang thai BOOKED.
9. Appointment ghi status history va outbox event.
10. Notification gui xac nhan.
11. Receptionist xem lich va confirm neu can.

## 11. Luong Chuan Co Thanh Toan Giu Cho
1. Patient chon slot va tao appointment.
2. Appointment reserve slot.
3. Appointment tao PENDING_PAYMENT.
4. Payment init giao dich.
5. Payment callback success.
6. Appointment consume event payment.succeeded.
7. Appointment chuyen BOOKED va book slot.
8. Appointment ghi outbox event appointment.booked.
9. Notification gui xac nhan.

## 12. Luong Payment Fail Hoac Timeout
1. Appointment dang PENDING_PAYMENT.
2. Payment fail hoac qua TTL thanh toan.
3. Appointment chuyen CANCELLED.
4. Doctor Schedule release slot.
5. Appointment ghi status history va outbox event.
6. Notification co the gui thong bao dat lich khong thanh cong neu can.

## 13. Chi So Thanh Cong Phase 1
- Dang nhap va goi protected API qua gateway thanh cong.
- Tao ho so benh nhan va dat lich end-to-end thanh cong.
- Hai request dong thoi cung slot chi mot request thanh cong.
- Idempotency key hoat dong voi create appointment va payment callback.
- Appointment status khong chuyen sai state machine.
- Payment success/fail dong bo duoc appointment va slot.
- Notification khong gui trung khi event bi deliver lap.
- Log co request id de truy vet luong booking.
