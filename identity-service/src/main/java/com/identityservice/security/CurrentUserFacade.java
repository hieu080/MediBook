package com.identityservice.security;

import com.identityservice.exception.AuthErrorCode;
import com.identityservice.exception.IdentityException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
public class CurrentUserFacade {
    public CustomUserDetail getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IdentityException(AuthErrorCode.AUTHENTICATION_REQUIRED);
        }

        Object principal = authentication.getPrincipal();
        if (!(principal instanceof CustomUserDetail currentUser)) {
            throw new IdentityException(AuthErrorCode.AUTHENTICATION_REQUIRED);
        }

        return currentUser;
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
