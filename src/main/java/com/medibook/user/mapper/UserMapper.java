package com.medibook.user.mapper;

import com.medibook.user.dto.reponse.UserResponse;
import com.medibook.user.dto.request.CreateUserRequest;
import com.medibook.user.entity.User;

import java.time.LocalDateTime;

/**
 * UserMapper là lớp tiện ích được sử dụng để chuyển đổi giữa các đối tượng User và UserResponse.
 * Lớp này cung cấp các phương thức tĩnh để chuyển đổi từ User sang UserResponse và ngược lại,
 * giúp tách biệt logic chuyển đổi dữ liệu khỏi các lớp khác trong ứng dụng.
 *
 * @author hieu080
 * @version 1.0
 * @since 2026-05-28
 */
public class UserMapper {
    /**
     * Phương thức toResponse nhận một đối tượng User và chuyển đổi nó thành một đối tượng UserResponse.
     * @param user
     * @return
     *
     * @author hieu080
     * @version 1.0
     * @since 2026-05-28
     */
    public static UserResponse toResponse(User user){
        return UserResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .role(user.getRole())
                .status(user.getStatus())
                .createdAt(user.getCreatedAt())
                .build();
    }

        /**
        * Phương thức toEntity nhận các tham số cần thiết để tạo một đối tượng User mới.
        * @param request
        * @return
        *
        * @author hieu080
        * @version 1.0
        * @since 2026-05-28
        */
    public static User toEntity(CreateUserRequest request){
        User user = new User();
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setPassword(request.getPassword());
        user.setCreatedAt(LocalDateTime.now());
        return user;
    }
}
