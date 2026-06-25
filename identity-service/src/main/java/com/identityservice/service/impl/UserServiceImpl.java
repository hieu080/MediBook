package com.identityservice.service.impl;

import com.identityservice.dto.request.RegisterRequest;
import com.identityservice.dto.response.UserAdminResponse;
import com.identityservice.dto.response.UserBasicResponse;
import com.identityservice.dto.response.UserPrivateResponse;
import com.identityservice.entity.Role;
import com.identityservice.entity.User;
import com.identityservice.entity.UserRole;
import com.identityservice.exception.IdentityException;
import com.identityservice.exception.RoleErrorCode;
import com.identityservice.exception.UserErrorCode;
import com.identityservice.mapper.UserMapper;
import com.identityservice.repository.RoleRepository;
import com.identityservice.repository.UserRepository;
import com.identityservice.repository.UserRoleRepository;
import com.identityservice.security.CurrentUserFacade;
import com.identityservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
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
    public UserPrivateResponse createUser(RegisterRequest request) {
        validateRegisterRequest(request);

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

        List<UserRole> userRoles = loadUserRoles(savedUser);
        return userMapper.toPrivateResponse(savedUser, toRoleCodes(userRoles), resolveDefaultRoleCode(userRoles));
    }

    @Override
    public UserBasicResponse getBasicUserDetailByPublicId(UUID publicId) {
        User user = userRepository.findByPublicIdAndDeletedAtIsNull(publicId)
                .orElseThrow(() -> new IdentityException(UserErrorCode.USER_NOT_FOUND));

        List<UserRole> userRoles = loadUserRoles(user);
        return userMapper.toBasicResponse(user, toRoleCodes(userRoles));
    }

    @Override
    public UserBasicResponse getBasicUserDetailByEmail(String email) {
        User user = userRepository.findByEmailAndDeletedAtIsNull(email)
                .orElseThrow(() -> new IdentityException(UserErrorCode.USER_NOT_FOUND));

        List<UserRole> userRoles = loadUserRoles(user);
        return userMapper.toBasicResponse(user, toRoleCodes(userRoles));
    }

    @Override
    public UserPrivateResponse getMyUserDetail() {
        CurrentUserFacade currentUserFacade = new CurrentUserFacade();
        UUID publicId = currentUserFacade.getCurrentPublicId();

        User user = userRepository.findByPublicIdAndDeletedAtIsNull(publicId)
                .orElseThrow(() -> new IdentityException(UserErrorCode.USER_NOT_FOUND));

        List<UserRole> userRoles = loadUserRoles(user);
        return userMapper.toPrivateResponse(user, toRoleCodes(userRoles), resolveDefaultRoleCode(userRoles));
    }

    @Override
    public UserAdminResponse getAdminUserDetailByPublicId(UUID publicId) {
        User user = userRepository.findByPublicIdAndDeletedAtIsNull(publicId)
                .orElseThrow(() -> new IdentityException(UserErrorCode.USER_NOT_FOUND));

        List<UserRole> userRoles = loadUserRoles(user);
        return userMapper.toAdminResponse(user, toRoleCodes(userRoles));
    }

    @Override
    public UserAdminResponse getAdminUserDetailByEmail(String email) {
        User user = userRepository.findByEmailAndDeletedAtIsNull(email)
                .orElseThrow(() -> new IdentityException(UserErrorCode.USER_NOT_FOUND));

        List<UserRole> userRoles = loadUserRoles(user);
        return userMapper.toAdminResponse(user, toRoleCodes(userRoles));
    }


    private List<UserRole> loadUserRoles(User user) {
        return userRoleRepository.findAllByUserAndDeletedAtIsNull(user);
    }

    private List<String> toRoleCodes(List<UserRole> userRoles) {
        return userRoles.stream()
                .map(userRole -> userRole.getRole().getCode())
                .toList();
    }

    private String resolveDefaultRoleCode(List<UserRole> userRoles) {
        return userRoles.stream()
                .filter(UserRole::isDefaultRole)
                .findFirst()
                .or(() -> userRoles.stream().findFirst())
                .map(userRole -> userRole.getRole().getCode())
                .orElse(null);
    }


    private void validateRegisterRequest(RegisterRequest request) {
        if (!request.getPassword().equals(request.getRePassword())) {
            throw new IdentityException(UserErrorCode.PASSWORD_CONFIRMATION_MISMATCH);
        }

        if (userRepository.existsByEmailAndDeletedAtIsNull(request.getEmail())) {
            throw new IdentityException(UserErrorCode.USER_ALREADY_EXISTS);
        }
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
                .defaultRole(true)
                .createdAt(createdAt)
                .build();
        userRoleRepository.save(userRole);
    }
}
