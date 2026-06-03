package com.identityservice.service.impl;

import com.identityservice.dto.request.RegisterRequest;
import com.identityservice.dto.response.UserResponse;
import com.identityservice.entity.Role;
import com.identityservice.entity.User;
import com.identityservice.exception.IdentityException;
import com.identityservice.exception.RoleErrorCode;
import com.identityservice.exception.UserErrorCode;
import com.identityservice.mapper.UserMapper;
import com.identityservice.repository.RoleRepository;
import com.identityservice.repository.UserRepository;
import com.identityservice.repository.UserRoleRepository;
import com.identityservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
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
                com.identityservice.enums.UserStatus.ACTIVE,
                now,
                now
        );
        User savedUser = userRepository.save(user);
        assignDefaultPatientRole(savedUser, now);
        return userMapper.toResponse(savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserByPublicId(UUID publicId) {
        User user = userRepository.findByPublicIdAndDeletedAtIsNull(publicId)
                .orElseThrow(() -> new IdentityException(UserErrorCode.USER_NOT_FOUND));
        return userMapper.toResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserByEmail(String email) {
        User user = userRepository.findByEmailAndDeletedAtIsNull(email)
                .orElseThrow(() -> new IdentityException(UserErrorCode.USER_NOT_FOUND));
        return userMapper.toResponse(user);
    }

    private void assignDefaultPatientRole(User user, LocalDateTime createdAt) {
        Role patientRole = roleRepository.findByCode(com.identityservice.enums.UserRole.PATIENT.name())
                .orElseThrow(() -> new IdentityException(RoleErrorCode.ROLE_NOT_FOUND));

        if (userRoleRepository.existsByUserAndRoleAndDeletedAtIsNull(user, patientRole)) {
            return;
        }

        com.identityservice.entity.UserRole userRole = com.identityservice.entity.UserRole.builder()
                .user(user)
                .role(patientRole)
                .createdAt(createdAt)
                .build();
        userRoleRepository.save(userRole);
    }
}
