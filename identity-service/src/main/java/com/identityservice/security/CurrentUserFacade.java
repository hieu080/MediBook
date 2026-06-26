package com.identityservice.security;

import com.identityservice.exception.AuthErrorCode;
import com.identityservice.exception.IdentityException;
import com.identityservice.exception.UserErrorCode;
import com.identityservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Facade ho tro truy cap thong tin nguoi dung hien tai tu SecurityContext.
 *
 * @author hieu080
 * @since 2026-06
 * @version 1.0
 */
@Component
@RequiredArgsConstructor
public class CurrentUserFacade {
    private final UserRepository userRepository;

    public CustomUserDetail getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IdentityException(AuthErrorCode.AUTHENTICATION_REQUIRED);
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof CustomUserDetail currentUser) {
            return currentUser;
        }

        if (principal instanceof Jwt jwt) {
            String publicId = jwt.getClaimAsString("publicId");
            if (publicId != null && !publicId.isBlank()) {
                return userRepository.findByPublicIdAndDeletedAtIsNull(UUID.fromString(publicId))
                        .map(user -> toCurrentUser(user, authentication))
                        .orElseThrow(() -> new IdentityException(UserErrorCode.USER_NOT_FOUND));
            }

            String email = jwt.getClaimAsString("email");
            if (email != null && !email.isBlank()) {
                return userRepository.findByEmailAndDeletedAtIsNull(email)
                        .map(user -> toCurrentUser(user, authentication))
                        .orElseThrow(() -> new IdentityException(UserErrorCode.USER_NOT_FOUND));
            }
        }

        throw new IdentityException(AuthErrorCode.AUTHENTICATION_REQUIRED);
    }

    private CustomUserDetail toCurrentUser(com.identityservice.entity.User user, Authentication authentication) {
        return CustomUserDetail.builder()
                .id(user.getId())
                .publicId(user.getPublicId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .status(user.getStatus())
                .authorities(authentication.getAuthorities())
                .build();
    }

    public String getCurrentUserEmail() {
        return getCurrentUser().getEmail();
    }

    public Long getCurrentUserId() {
        return getCurrentUser().getId();
    }

    public UUID getCurrentPublicId() {
        return getCurrentUser().getPublicId();
    }
}
