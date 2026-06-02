package com.identityservice.repository;

import com.identityservice.entity.Role;
import com.identityservice.entity.User;
import com.identityservice.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository cho thực thể UserRole, cung cấp các phương thức truy vấn cơ bản.
 *
 * @author hieu080
 * @since 2026-06
 * @version 1.0
 */

@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, Long> {
    boolean existsByUserAndRoleAndDeletedAtIsNull(User user, Role role);
    List<UserRole> findAllByUserAndDeletedAtIsNull(User user);
    List<UserRole> findAllByUserIdAndDeletedAtIsNull(Long userId);
    Optional<UserRole> findByUserAndRoleAndDeletedAtIsNull(User user, Role role);
}
