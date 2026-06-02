package com.identityservice.dto.response;

import com.identityservice.enums.UserStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO phản hồi thông tin người dùng trong identity-service.
 *
 * @author hieu080
 * @since 2026-06
 * @version 1.0
 */
@Getter
@Builder
public class UserResponse {
    private UUID publicId;
    private String fullName;
    private String email;
    private String phoneNumber;
    private UserStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
