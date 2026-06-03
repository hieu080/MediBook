package com.identityservice.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * DTO yêu cầu đăng nhập.
 * Chứa thông tin cần thiết để xác thực người dùng và cấp quyền truy cập.
 *
 * @author hieu080
 * @since 2026-06
 * @version 1.0
 */

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {
    @Email(message = "Email không hợp lệ")
    @NotBlank(message = "Email không được để trống")
    private String email;

    @Size(min = 8 , message = "Mật khẩu phải có ít nhất 8 ký tự")
    @NotBlank(message = "Mật khẩu không được để trống")
    private String password;
}
