package com.medibook.user.dto.reponse;

import com.medibook.user.enums.UserRole;
import com.medibook.user.enums.UserStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * UserResponse là lớp DTO (Data Transfer Object) được sử dụng để trả về thông tin của một người dùng
 * sau khi đã được tạo hoặc truy vấn.
 * Lớp này bao gồm các trường thông tin như
 * id, tên đầy đủ, email, số điện thoại, vai trò, trạng thái và thời gian tạo.
 *
 * @author hieu080
 * @version 1.0
 * @since 2026-05-28
 */

@Getter
@Builder
public class UserResponse {
    private Long id;
    private String fullName;
    private String email;
    private String phoneNumber;
    private UserRole role;
    private UserStatus status;
    private LocalDateTime createdAt;
}
