package com.identityservice.repository;

import com.identityservice.entity.RefreshToken;
import com.identityservice.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository cho thực thể RefreshToken, cung cấp các phương thức truy vấn cơ bản.
 *
 * @author hieu080
 * @since 2026-06
 * @version 1.0
 */

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByTokenHash(String tokenHash);
    List<RefreshToken> findAllByUserAndRevokedAtIsNull(User user);
    Optional<RefreshToken> findByTokenHashAndRevokedAtIsNull(String tokenHash);
    boolean existsByTokenHash(String tokenHash);
}
