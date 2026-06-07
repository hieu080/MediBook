package com.patientservice.mapper;

import com.patientservice.dto.request.CreatePatientRequest;
import com.patientservice.dto.request.UpdatePatientRequest;
import com.patientservice.dto.response.PatientAdminResponse;
import com.patientservice.dto.response.PatientBasicResponse;
import com.patientservice.dto.response.PatientPrivateResponse;
import com.patientservice.entity.Patient;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Mapper chuyển đổi dữ liệu liên quan đến Patient giữa entity và DTO.
 *
 * @author hieu080
 * @since 2026-06
 * @version 1.0
 */
@Component
public class PatientMapper {

    public Patient toEntity(CreatePatientRequest request, UUID publicId, LocalDateTime createdAt) {
        return Patient.builder()
                .publicId(publicId)
                .publicUserId(request.getPublicUserId())
                .fullName(request.getFullName())
                .dateOfBirth(request.getDateOfBirth())
                .gender(request.getGender())
                .phoneNumber(request.getPhoneNumber())
                .email(request.getEmail())
                .address(request.getAddress())
                .insuranceNumber(request.getInsuranceNumber())
                .emergencyContactName(request.getEmergencyContactName())
                .emergencyContactPhone(request.getEmergencyContactPhone())
                .createdAt(createdAt)
                .build();
    }

    public void applyUpdate(Patient patient, UpdatePatientRequest request, LocalDateTime updatedAt) {
        if (request.getFullName() != null) {
            patient.setFullName(request.getFullName());
        }
        if (request.getDateOfBirth() != null) {
            patient.setDateOfBirth(request.getDateOfBirth());
        }
        if (request.getGender() != null) {
            patient.setGender(request.getGender());
        }
        if (request.getPhoneNumber() != null) {
            patient.setPhoneNumber(request.getPhoneNumber());
        }
        if (request.getEmail() != null) {
            patient.setEmail(request.getEmail());
        }
        if (request.getAddress() != null) {
            patient.setAddress(request.getAddress());
        }
        if (request.getInsuranceNumber() != null) {
            patient.setInsuranceNumber(request.getInsuranceNumber());
        }
        if (request.getEmergencyContactName() != null) {
            patient.setEmergencyContactName(request.getEmergencyContactName());
        }
        if (request.getEmergencyContactPhone() != null) {
            patient.setEmergencyContactPhone(request.getEmergencyContactPhone());
        }
        patient.setUpdatedAt(updatedAt);
    }

    public PatientBasicResponse toBasicResponse(Patient patient) {
        return PatientBasicResponse.builder()
                .publicId(patient.getPublicId())
                .fullName(patient.getFullName())
                .dateOfBirth(patient.getDateOfBirth())
                .gender(patient.getGender())
                .phoneNumber(patient.getPhoneNumber())
                .build();
    }

    public PatientPrivateResponse toPrivateResponse(Patient patient) {
        return PatientPrivateResponse.builder()
                .publicId(patient.getPublicId())
                .publicUserId(patient.getPublicUserId())
                .fullName(patient.getFullName())
                .dateOfBirth(patient.getDateOfBirth())
                .gender(patient.getGender())
                .phoneNumber(patient.getPhoneNumber())
                .email(patient.getEmail())
                .address(patient.getAddress())
                .insuranceNumber(patient.getInsuranceNumber())
                .emergencyContactName(patient.getEmergencyContactName())
                .emergencyContactPhone(patient.getEmergencyContactPhone())
                .createdAt(patient.getCreatedAt())
                .updatedAt(patient.getUpdatedAt())
                .build();
    }

    public PatientAdminResponse toAdminResponse(Patient patient) {
        return PatientAdminResponse.builder()
                .id(patient.getId())
                .publicId(patient.getPublicId())
                .publicUserId(patient.getPublicUserId())
                .fullName(patient.getFullName())
                .dateOfBirth(patient.getDateOfBirth())
                .gender(patient.getGender())
                .phoneNumber(patient.getPhoneNumber())
                .email(patient.getEmail())
                .address(patient.getAddress())
                .insuranceNumber(patient.getInsuranceNumber())
                .emergencyContactName(patient.getEmergencyContactName())
                .emergencyContactPhone(patient.getEmergencyContactPhone())
                .createdAt(patient.getCreatedAt())
                .updatedAt(patient.getUpdatedAt())
                .deletedAt(patient.getDeletedAt())
                .build();
    }
}
