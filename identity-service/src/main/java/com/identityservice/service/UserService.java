package com.identityservice.service;

import com.identityservice.dto.request.RegisterRequest;
import com.identityservice.dto.response.UserAdminResponse;
import com.identityservice.dto.response.UserBasicResponse;
import com.identityservice.dto.response.UserPrivateResponse;

import java.util.UUID;

/**
 * Interface định nghĩa các thao tác nghiệp vụ liên quan đến người dùng.
 *
 * @author hieu080
 * @since 2026-06
 * @version 1.0
 */
public interface UserService {

    UserPrivateResponse createUser(RegisterRequest request);

    UserBasicResponse getBasicUserDetailByPublicId(UUID publicId);

    UserBasicResponse getBasicUserDetailByEmail(String email);

    UserPrivateResponse getMyUserDetail();

    UserAdminResponse getAdminUserDetailByPublicId(UUID publicId);

    UserAdminResponse getAdminUserDetailByEmail(String email);
}
