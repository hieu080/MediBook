package com.identityservice.security;

import com.identityservice.entity.User;
import com.identityservice.exception.UserErrorCode;
import com.identityservice.repository.UserRepository;
import com.identityservice.repository.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * UserDetailsService tùy biến dùng để Spring Security tải thông tin người dùng từ cơ sở dữ liệu.
 *
 * @author hieu080
 * @since 2026-06
 * @version 1.0
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmailAndDeletedAtIsNull(email)
                .orElseThrow(() -> new UsernameNotFoundException(UserErrorCode.USER_NOT_FOUND.defaultDetail()));

        List<String> roles = userRoleRepository.findAllByUserAndDeletedAtIsNull(user)
                .stream()
                .map(userRole -> userRole.getRole().getCode())
                .toList();

        List<SimpleGrantedAuthority> authorities = (roles.isEmpty() ? List.of("PATIENT") : roles)
                .stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .toList();

        return CustomUserDetail.builder()
                .id(user.getId())
                .email(user.getEmail())
                .passwordHash(user.getPasswordHash())
                .publicId(user.getPublicId())
                .fullName(user.getFullName())
                .authorities(authorities)
                .status(user.getStatus())
                .build();
    }
}
