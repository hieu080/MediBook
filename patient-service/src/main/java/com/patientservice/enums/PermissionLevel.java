package com.patientservice.enums;

/**
 * Cấp độ quyền hạn của người dùng liên quan đến bệnh nhân.
 * <ul>
 *     <li>OWNER: Chủ sở hữu, có toàn quyền truy cập và quản lý thông tin bệnh nhân.</li>
 *     <li>MANAGER: Quản lý, có quyền chỉnh sửa thông tin bệnh nhân nhưng không thể xóa.</li>
 *     <li>VIEWER: Người xem, chỉ có quyền xem thông tin bệnh nhân mà không thể chỉnh sửa.</li>
 * </ul>
 *
 * @author hieu080
 * @since 2026-06
 * @version 1.0
 */

public enum PermissionLevel {
    OWNER,
    MANAGER,
    VIEWER
}
