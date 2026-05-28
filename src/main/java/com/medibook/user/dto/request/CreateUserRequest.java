package com.medibook.user.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * CreateUserRequest là lớp DTO (Data Transfer Object) được sử dụng để nhận dữ liệu khi tạo mới một người dùng.
 * Lớp này bao gồm các trường thông tin cần thiết như tên đầy đủ, email, số điện thoại và mật khẩu,
 * cùng với các ràng buộc xác thực để đảm bảo dữ liệu hợp lệ.
 *
 * @author hieu080
 * @version 1.0
 * @since 2026-05-28
 */

@Getter
@Setter
public class CreateUserRequest {
    @NotBlank(message = "Full name is required")
    private String fullName;

    @Email(message = "Email should be valid")
    @NotBlank(message = "Email is required")
    private String email;

    @NotBlank(message = "Phone number is required")
    private String phoneNumber;

    @NotBlank(message = "Password is required")
    @Size(min= 6, message = "Password must be at least 6 characters long")
    private String password;
}
