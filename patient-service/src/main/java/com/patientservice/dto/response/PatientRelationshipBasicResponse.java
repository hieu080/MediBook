package com.patientservice.dto.response;

import com.patientservice.enums.PermissionLevel;
import com.patientservice.enums.RelationshipType;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

/**
 * DTO phản hồi thông tin mối quan hệ bệnh nhân cơ bản.
 *
 * @author hieu080
 * @since 2026-06
 * @version 1.0
 */
@Getter
@Builder
public class PatientRelationshipBasicResponse {
    private UUID publicId;
    private UUID patientPublicId;
    private UUID relatedUserPublicId;
    private RelationshipType relationshipType;
    private PermissionLevel permissionLevel;
    private boolean primaryContact;
}
