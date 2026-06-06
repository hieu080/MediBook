package com.identityservice.dto.response;

import com.identityservice.enums.UserStatus;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

/**
 * DTO phản hồi thông tin người dùng ở mức cơ bản cho người dùng khác xem.
 */
@Getter
@Builder
public class UserBasicResponse {
    private UUID publicId;
    private String fullName;
    private UserStatus status;
    private List<String> roles;
}
