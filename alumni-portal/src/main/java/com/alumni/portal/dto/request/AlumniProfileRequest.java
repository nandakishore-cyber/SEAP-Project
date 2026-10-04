package com.alumni.portal.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class AlumniProfileRequest {

    @NotBlank(message = "College name is required")
    @Size(max = 100)
    private String collegeName;

    @NotBlank(message = "Department is required")
    @Size(max = 100)
    private String department;

    @NotBlank(message = "Degree is required")
    @Size(max = 100)
    private String degree;

    @NotNull(message = "Graduation year is required")
    @Min(value = 1950, message = "Graduation year must be after 1950")
    @Max(value = 2100, message = "Graduation year must be before 2100")
    private Integer graduationYear;

    @Size(max = 50)
    private String rollNumber;

    @Size(max = 150)
    private String currentCompany;

    @Size(max = 100)
    private String currentDesignation;

    @Size(max = 100)
    private String industry;

    @Min(value = 0, message = "Years of experience cannot be negative")
    private Integer yearsOfExperience;

    @Size(max = 1000, message = "Skills must not exceed 1000 characters")
    private String skills;
}
