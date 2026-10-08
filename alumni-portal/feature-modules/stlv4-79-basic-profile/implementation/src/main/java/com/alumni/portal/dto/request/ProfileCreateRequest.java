package com.alumni.portal.dto.request;

import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class ProfileCreateRequest {

    @Size(max = 20, message = "Phone number must not exceed 20 characters")
    private String phone;

    private LocalDate dateOfBirth;

    @Size(max = 200)
    private String address;

    @Size(max = 100)
    private String city;

    @Size(max = 100)
    private String state;

    @Size(max = 100)
    private String country;

    @Size(max = 500, message = "Bio must not exceed 500 characters")
    private String bio;

    @Size(max = 300)
    private String profilePictureUrl;

    @Size(max = 300)
    private String linkedinUrl;

    @Size(max = 100)
    private String studentId;

    @Size(max = 100)
    private String program;

    @Size(max = 100)
    private String department;

    private Integer expectedGraduationYear;

    @Size(max = 1000)
    private String interests;

    @Size(max = 1000)
    private String studentSkills;
}
