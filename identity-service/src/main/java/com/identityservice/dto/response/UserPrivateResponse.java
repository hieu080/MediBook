package com.identityservice.dto.response;

import com.identityservice.enums.UserStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * DTO phản hồi thông tin người dùng cho chính chủ tài khoản xem.
 */
@Getter
@Builder
public class UserPrivateResponse {
    private UUID publicId;
    private String fullName;
    private String email;
    private String phoneNumber;
    private UserStatus status;
    private List<String> roles;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
