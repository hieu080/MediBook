package com.identityservice.service;

import com.identityservice.dto.request.RegisterRequest;
import com.identityservice.dto.response.UserResponse;

import java.util.UUID;

/**
 * Interface định nghĩa các thao tác nghiệp vụ liên quan đến người dùng.
 *
 * @author hieu080
 * @since 2026-06
 * @version 1.0
 */
public interface UserService {

    UserResponse createUser(RegisterRequest request);

    UserResponse getUserByPublicId(UUID publicId);

    UserResponse getUserByEmail(String email);
}
