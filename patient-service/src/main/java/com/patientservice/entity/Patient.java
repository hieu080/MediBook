package com.patientservice.entity;

import com.patientservice.enums.Gender;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Bệnh nhân trong hệ thống.
 *
 * @author hieu080
 * @since 2026-06
 * @version 1.0
 */

@Entity
@Table(name = "patients")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Patient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "public_id", nullable = false, unique = true, updatable = false)
    private UUID publicId;

    @Column(name = "user_id", nullable = true, unique = true, updatable = false)
    private UUID publicUserId;

    @Column(name = "full_name", nullable = false, length = 150)
    private String fullName;

    @Column(name = "date_of_birth", nullable = false)
    private LocalDate dateOfBirth;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", nullable = false)
    private Gender gender;

    @Column(name = "phone_number", nullable = true, length = 20)
    private String phoneNumber;

    @Column(name = "email", nullable = true, length = 100)
    private String email;

    @Column(name = "address", nullable = true, length = 255)
    private String address;

    @Column(name = "insurance_number", nullable = true, length = 50)
    private String insuranceNumber;

    @Column(name = "emergency_contact_name", nullable = true, length = 150)
    private String emergencyContactName;

    @Column(name = "emergency_contact_phone", nullable = true, length = 20)
    private String emergencyContactPhone;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}
