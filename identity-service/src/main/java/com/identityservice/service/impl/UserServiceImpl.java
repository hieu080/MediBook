package com.identityservice.service.impl;

import com.identityservice.dto.request.RegisterRequest;
import com.identityservice.dto.response.UserResponse;
import com.identityservice.entity.User;
import com.identityservice.enums.UserStatus;
import com.identityservice.exception.IdentityException;
import com.identityservice.exception.UserErrorCode;
import com.identityservice.mapper.UserMapper;
import com.identityservice.repository.UserRepository;
import com.identityservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Triển khai các nghiệp vụ liên quan đến người dùng trong identity-service.
 *
 * @author hieu080
 * @since 2026-06
 * @version 1.0
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    @Override
    public UserResponse createUser(RegisterRequest request) {
        if (userRepository.existsByEmailAndDeletedAtIsNull(request.getEmail())) {
            throw new IdentityException(UserErrorCode.USER_ALREADY_EXISTS);
        }

        LocalDateTime now = LocalDateTime.now();
        User user = userMapper.toEntity(
                request,
                passwordEncoder.encode(request.getPassword()),
                UUID.randomUUID(),
                UserStatus.ACTIVE,
                now,
                now
        );

        User savedUser = userRepository.save(user);
        return userMapper.toResponse(savedUser);
    }

    @Override
    public UserResponse getUserByPublicId(UUID publicId) {
        User user = userRepository.findByPublicIdAndDeletedAtIsNull(publicId)
                .orElseThrow(() -> new IdentityException(UserErrorCode.USER_NOT_FOUND));
        return userMapper.toResponse(user);
    }

    @Override
    public UserResponse getUserByEmail(String email) {
        User user = userRepository.findByEmailAndDeletedAtIsNull(email)
                .orElseThrow(() -> new IdentityException(UserErrorCode.USER_NOT_FOUND));
        return userMapper.toResponse(user);
    }
}
