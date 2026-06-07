package com.patientservice.dto.response;

import com.patientservice.enums.Gender;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.UUID;

/**
 * DTO phản hồi thông tin bệnh nhân cơ bản.
 *
 * @author hieu080
 * @since 2026-06
 * @version 1.0
 */
@Getter
@Builder
public class PatientBasicResponse {
    private UUID publicId;
    private String fullName;
    private LocalDate dateOfBirth;
    private Gender gender;
    private String phoneNumber;
}
