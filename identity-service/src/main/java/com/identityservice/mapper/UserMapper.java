package com.identityservice.mapper;

import com.identityservice.dto.request.RegisterRequest;
import com.identityservice.dto.response.UserAdminResponse;
import com.identityservice.dto.response.UserBasicResponse;
import com.identityservice.dto.response.UserPrivateResponse;
import com.identityservice.entity.User;
import com.identityservice.enums.UserStatus;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
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
                .phoneNumber(normalizeOptionalText(request.getPhoneNumber()))
                .passwordHash(passwordHash)
                .status(status)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();
    }

    private String normalizeOptionalText(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    public UserBasicResponse toBasicResponse(User user, List<String> roles) {
        return UserBasicResponse.builder()
                .publicId(user.getPublicId())
                .fullName(user.getFullName())
                .status(user.getStatus())
                .roles(roles)
                .build();
    }

    public UserPrivateResponse toPrivateResponse(User user, List<String> roles) {
        return UserPrivateResponse.builder()
                .publicId(user.getPublicId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .status(user.getStatus())
                .roles(roles)
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    public UserAdminResponse toAdminResponse(User user, List<String> roles) {
        return UserAdminResponse.builder()
                .id(user.getId())
                .publicId(user.getPublicId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .status(user.getStatus())
                .roles(roles)
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .deletedAt(user.getDeletedAt())
                .build();
    }
}
