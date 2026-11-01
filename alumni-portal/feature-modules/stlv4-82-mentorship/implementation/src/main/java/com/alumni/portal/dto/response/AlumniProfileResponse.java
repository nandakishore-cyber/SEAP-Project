package com.alumni.portal.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class AlumniProfileResponse {

    private Long id;
    private Long userId;
    private String firstName;
    private String lastName;
    private String email;

    // Academic
    private String collegeName;
    private String department;
    private String degree;
    private Integer graduationYear;
    private String rollNumber;

    // Professional
    private String currentCompany;
    private String currentDesignation;
    private String industry;
    private Integer yearsOfExperience;
    private String skills;

    // Verification
    private String verificationStatus;
    private String verificationRemarks;
    private LocalDateTime verifiedAt;

    // Audit
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
