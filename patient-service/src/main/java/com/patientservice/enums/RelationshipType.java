package com.patientservice.enums;

/**
 * Loại quan hệ giữa bệnh nhân và người dùng liên quan.
 * <ul>
 *     <li>PARENT: Cha mẹ</li>
 *     <li>CHILD: Con cái</li>
 *     <li>SPOUSE: Vợ/chồng</li>
 *     <li>GUARDIAN: Người giám hộ</li>
 *     <li>CAREGIVER: Người chăm sóc</li>
 *     <li>SELF: Bản thân</li>
 *     <li>OTHER: Quan hệ khác</li>
 * </ul>
 *
 * @author hieu080
 * @since 2026-06
 * @version 1.0
 */

public enum RelationshipType {
    PARENT,
    CHILD,
    SPOUSE,
    GUARDIAN,
    CAREGIVER,
    SELF,
    OTHER
}
