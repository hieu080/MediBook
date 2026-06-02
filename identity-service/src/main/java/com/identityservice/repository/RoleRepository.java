package com.identityservice.repository;

import com.identityservice.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Repository cho thực thể Role, cung cấp các phương thức truy vấn cơ bản.
 *
 * @author hieu080
 * @since 2026-06
 * @version 1.0
 */

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByCode(String code);
    boolean existsByCode(String code);
    List<Role> findAllByCodeIn(Collection<String> codes);
}
