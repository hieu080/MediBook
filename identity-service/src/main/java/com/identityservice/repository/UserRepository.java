package com.identityservice.repository;

import com.identityservice.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository cho thực thể User, cung cấp các phương thức truy vấn cơ bản.
 *
 * @author hieu080
 * @since 2026-06
 * @version 1.0
 */

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByIdAndDeletedAtIsNull(Long id);
    Optional<User> findByPublicIdAndDeletedAtIsNull(UUID publicId);
    Optional<User> findByFullNameAndDeletedAtIsNull(String fullName);
    Optional<User> findByEmailAndDeletedAtIsNull(String email);
    List<User> findAllByFullNameContainingIgnoreCaseAndDeletedAtIsNull(String fullName);
    Optional<User> findByPhoneNumberAndDeletedAtIsNull(String phoneNumber);
    boolean existsByEmailAndDeletedAtIsNull(String email);
    boolean existsByPhoneNumberAndDeletedAtIsNull(String phoneNumber);
}
