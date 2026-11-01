package com.alumni.portal.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MentorshipRequestCreateRequest {

    @NotNull(message = "Alumni profile is required")
    private Long alumniProfileId;

    @NotBlank(message = "Mentorship message is required")
    @Size(max = 1000)
    private String message;

    @Size(max = 500)
    private String goals;
}
