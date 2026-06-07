package com.patientservice.mapper;

import com.patientservice.dto.request.CreatePatientRelationshipRequest;
import com.patientservice.dto.request.UpdatePatientRelationshipRequest;
import com.patientservice.dto.response.PatientRelationshipAdminResponse;
import com.patientservice.dto.response.PatientRelationshipBasicResponse;
import com.patientservice.entity.PatientRelationship;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Mapper chuyển đổi dữ liệu liên quan đến PatientRelationship giữa entity và DTO.
 *
 * @author hieu080
 * @since 2026-06
 * @version 1.0
 */
@Component
public class PatientRelationshipMapper {

    public PatientRelationship toEntity(CreatePatientRelationshipRequest request, UUID publicId, LocalDateTime createdAt) {
        return PatientRelationship.builder()
                .publicId(publicId)
                .patientPublicId(request.getPatientPublicId())
                .relatedUserPublicId(request.getRelatedUserPublicId())
                .relationshipType(request.getRelationshipType())
                .permissionLevel(request.getPermissionLevel())
                .isPrimaryContact(request.isPrimaryContact())
                .createdAt(createdAt)
                .build();
    }

    public void applyUpdate(PatientRelationship relationship, UpdatePatientRelationshipRequest request, LocalDateTime updatedAt) {
        if (request.getRelationshipType() != null) {
            relationship.setRelationshipType(request.getRelationshipType());
        }
        if (request.getPermissionLevel() != null) {
            relationship.setPermissionLevel(request.getPermissionLevel());
        }
        if (request.getPrimaryContact() != null) {
            relationship.setPrimaryContact(request.getPrimaryContact());
        }
        relationship.setUpdatedAt(updatedAt);
    }

    public PatientRelationshipBasicResponse toBasicResponse(PatientRelationship relationship) {
        return PatientRelationshipBasicResponse.builder()
                .publicId(relationship.getPublicId())
                .patientPublicId(relationship.getPatientPublicId())
                .relatedUserPublicId(relationship.getRelatedUserPublicId())
                .relationshipType(relationship.getRelationshipType())
                .permissionLevel(relationship.getPermissionLevel())
                .primaryContact(relationship.isPrimaryContact())
                .build();
    }

    public PatientRelationshipAdminResponse toAdminResponse(PatientRelationship relationship) {
        return PatientRelationshipAdminResponse.builder()
                .id(relationship.getId())
                .publicId(relationship.getPublicId())
                .patientPublicId(relationship.getPatientPublicId())
                .relatedUserPublicId(relationship.getRelatedUserPublicId())
                .relationshipType(relationship.getRelationshipType())
                .permissionLevel(relationship.getPermissionLevel())
                .primaryContact(relationship.isPrimaryContact())
                .createdAt(relationship.getCreatedAt())
                .updatedAt(relationship.getUpdatedAt())
                .deletedAt(relationship.getDeletedAt())
                .build();
    }
}
