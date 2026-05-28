package com.medibook.user.entity;

import com.medibook.user.enums.UserRole;
import com.medibook.user.enums.UserStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * User là thực thể đại diện cho người dùng trong hệ thống,
 * bao gồm các thông tin cơ bản như tên,
 * email, số điện thoại, mật khẩu, vai trò và trạng thái.
 *
 * @author hieu080
 * @version 1.0
 * @since 2026-05-28
 */

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="full_name", nullable = false, length = 100)
    private String fullName;

    @Column(name="email", nullable = false, unique = true, length = 100)
    private String email;

    @Column(name="phone_number", nullable = false, length = 20)
    private String phoneNumber;

    @Column(name="password", nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name="role", nullable = false)
    private UserRole role;

    @Enumerated(EnumType.STRING)
    @Column(name="status", nullable = false)
    private UserStatus status;

    @Column(name="created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name="updated_at")
    private LocalDateTime updatedAt;
}
