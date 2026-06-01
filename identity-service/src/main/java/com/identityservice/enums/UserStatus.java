package com.identityservice.enums;

/**
 * Trạng thái người dùng trong hệ thống.
 * <ul>
 *     <li>PENDING_VERIFICATION: Tài khoản mới đăng ký nhưng chưa xác thực email hoặc chưa được duyệt</li>
 *     <li>ACTIVE: Hoạt động bình thường</li>
 *     <li>INACTIVE: Tạm ngừng sử dụng, có thể kích hoạt lại</li>
 *     <li>SUSPENDED: Bị khóa do vi phạm hoặc bị Admin khóa</li>
 * </ul>
 *
 * @author hieu080
 * @since 2026-06
 * @version 1.0
 */
public enum UserStatus {
    PENDING_VERIFICATION,
    ACTIVE,
    INACTIVE,
    SUSPENDED
}
