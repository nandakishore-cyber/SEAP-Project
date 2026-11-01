package com.alumni.portal.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventRegistrationRequest {

    @NotBlank
    @Size(max = 30)
    private String attendanceMode;

    @NotBlank
    @Size(max = 100)
    private String currentSemester;

    @NotBlank
    @Size(max = 500)
    private String expectations;

    @Size(max = 100)
    private String dietaryPreference;
}
