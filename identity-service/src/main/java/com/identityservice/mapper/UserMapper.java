package com.identityservice.mapper;

import com.identityservice.dto.request.RegisterRequest;
import com.identityservice.dto.response.UserResponse;
import com.identityservice.entity.User;
import com.identityservice.enums.UserStatus;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Mapper chuyển đổi dữ liệu liên quan đến User giữa entity và DTO.
 *
 * @author hieu080
 * @since 2026-06
 * @version 1.0
 */
@Component
public class UserMapper {

    public User toEntity(
            RegisterRequest request,
            String passwordHash,
            UUID publicId,
            UserStatus status,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        return User.builder()
                .publicId(publicId)
                .fullName(request.getFullName())
                .email(request.getEmail())
                .passwordHash(passwordHash)
                .status(status)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();
    }

    public UserResponse toResponse(User user) {
        return UserResponse.builder()
                .publicId(user.getPublicId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .status(user.getStatus())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
