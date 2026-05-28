package com.medibook.user.repository;

import com.medibook.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * UserRepository là giao diện mở rộng từ JpaRepository,
 * cung cấp các phương thức để truy xuất và quản lý dữ liệu người dùng trong cơ sở dữ liệu.
 * Giao diện này bao gồm các phương thức tùy chỉnh để tìm kiếm người dùng theo email và số điện thoại,
 * cũng như kiểm tra sự tồn tại của chúng.
 *
 * @author hieu080
 * @version 1.0
 * @since 2026-05-28
 */
public interface UserRepository extends JpaRepository<User, Long> {
    /**
     * Tìm kiếm người dùng theo email.
     * @param email
     * @return Optional chứa người dùng nếu tìm thấy, hoặc rỗng nếu không tìm thấy.
     *
     * @author hieu080
     * @version 1.0
     * @since 2026-05-28
     */
    Optional<User> findByEmail(String email);

    /**
     * Tìm kiếm người dùng theo số điện thoại.
     * @param phoneNumber
     * @return Optional chứa người dùng nếu tìm thấy, hoặc rỗng nếu không tìm thấy.
     *
     * @author hieu080
     * @version 1.0
     * @since 2026-05-28
     */
    Optional<User> findByPhoneNumber(String phoneNumber);

    /**
     * Kiểm tra sự tồn tại của người dùng theo email.
     * @param email
     * @return true nếu người dùng tồn tại, false nếu không tồn tại.
     *
     * @author hieu080
     * @version 1.0
     * @since 2026-05-28
     */
    boolean existsByEmail(String email);

    /**
     * Kiểm tra sự tồn tại của người dùng theo số điện thoại.
     * @param phoneNumber
     * @return true nếu người dùng tồn tại, false nếu không tồn tại.
     *
     * @author hieu080
     * @version 1.0
     * @since 2026-05-28
     */
    boolean existsByPhoneNumber(String phoneNumber);
}
