package com.alumni.portal.dto.request;

import com.alumni.portal.entity.enums.MentorshipRequestStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MentorshipRequestDecisionRequest {

    @NotNull(message = "Decision status is required")
    private MentorshipRequestStatus status;

    @Size(max = 1000)
    private String responseMessage;
}
