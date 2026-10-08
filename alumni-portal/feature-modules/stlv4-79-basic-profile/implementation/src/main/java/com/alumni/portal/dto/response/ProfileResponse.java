package com.alumni.portal.dto.response;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class ProfileResponse {

    private Long id;
    private Long userId;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private LocalDate dateOfBirth;
    private String address;
    private String city;
    private String state;
    private String country;
    private String bio;
    private String profilePictureUrl;
    private String linkedinUrl;
    private String studentId;
    private String program;
    private String department;
    private Integer expectedGraduationYear;
    private String interests;
    private String studentSkills;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
