package com.identityservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * DTO yêu cầu thay đổi vai trò mặc định của người dùng hiện tại.
 *
 * @author hieu080
 * @since 2026-06
 * @version 1.0
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChangeDefaultRoleRequest {
    @NotBlank(message = "Vai trò mặc định không được để trống")
    private String role;
}
