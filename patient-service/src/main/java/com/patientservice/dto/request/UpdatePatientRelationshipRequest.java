package com.patientservice.dto.request;

import com.patientservice.enums.PermissionLevel;
import com.patientservice.enums.RelationshipType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO yêu cầu cập nhật thông tin mối quan hệ bệnh nhân.
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
public class UpdatePatientRelationshipRequest {
    private RelationshipType relationshipType;
    private PermissionLevel permissionLevel;
    private Boolean primaryContact;
}
