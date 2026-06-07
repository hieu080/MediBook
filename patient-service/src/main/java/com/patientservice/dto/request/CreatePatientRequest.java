package com.patientservice.dto.request;

import com.patientservice.enums.Gender;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

/**
 * DTO yêu cầu tạo bệnh nhân mới. Chứa các thông tin cần thiết để tạo một bệnh nhân trong hệ thống.
 *
 * @author hieu080
 * @since 2026-06
 * @version 1.0
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePatientRequest {
    private UUID publicUserId;

    @NotBlank(message = "Họ tên bệnh nhân không được để trống")
    @Size(max = 150, message = "Họ tên bệnh nhân không được vượt quá 150 ký tự")
    private String fullName;

    @NotNull(message = "Ngày sinh không được để trống")
    private LocalDate dateOfBirth;

    @NotNull(message = "Giới tính không được để trống")
    private Gender gender;

    @Size(max = 20, message = "Số điện thoại không được vượt quá 20 ký tự")
    private String phoneNumber;

    @Email(message = "Email không hợp lệ")
    @Size(max = 100, message = "Email không được vượt quá 100 ký tự")
    private String email;

    @Size(max = 255, message = "Địa chỉ không được vượt quá 255 ký tự")
    private String address;

    @Size(max = 50, message = "Số BHYT không được vượt quá 50 ký tự")
    private String insuranceNumber;

    @Size(max = 150, message = "Tên người liên hệ khẩn cấp không được vượt quá 150 ký tự")
    private String emergencyContactName;

    @Size(max = 20, message = "Số điện thoại liên hệ khẩn cấp không được vượt quá 20 ký tự")
    private String emergencyContactPhone;
}
