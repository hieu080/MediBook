package com.medibook.user.service;

import com.medibook.user.dto.reponse.UserResponse;
import com.medibook.user.dto.request.CreateUserRequest;

/**
 * UserService là giao diện định nghĩa các phương thức liên quan đến quản lý người dùng trong hệ thống.
 *
 * @author hieu080
 * @version 1.0
 * @since 2026-05-28
 */
public interface UserService {
    UserResponse getUserById(Long id);
    UserResponse createUser(CreateUserRequest request);
}
