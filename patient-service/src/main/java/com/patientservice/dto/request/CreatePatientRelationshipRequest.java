package com.patientservice.dto.request;

import com.patientservice.enums.PermissionLevel;
import com.patientservice.enums.RelationshipType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/**
 * DTO yêu cầu tạo mối quan hệ giữa bệnh nhân và người thân.
 * Được sử dụng khi cần thiết lập mối quan hệ mới giữa bệnh nhân và người thân
 * trong hệ thống quản lý bệnh nhân.
 *
 * @author hieu080
 * @since 2026-06
 * @version 1.0
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePatientRelationshipRequest {
    @NotNull(message = "Hồ sơ bệnh nhân không được để trống")
    private UUID patientPublicId;

    @NotNull(message = "Người liên quan không được để trống")
    private UUID relatedUserPublicId;

    @NotNull(message = "Loại quan hệ không được để trống")
    private RelationshipType relationshipType;

    @NotNull(message = "Cấp quyền không được để trống")
    private PermissionLevel permissionLevel;

    @Builder.Default
    private boolean primaryContact = false;
}
