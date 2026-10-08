package com.alumni.portal.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class VerificationRequest {

    @NotNull(message = "Approved flag is required")
    private Boolean approved;

    @NotBlank(message = "Remarks are required")
    private String remarks;
}
