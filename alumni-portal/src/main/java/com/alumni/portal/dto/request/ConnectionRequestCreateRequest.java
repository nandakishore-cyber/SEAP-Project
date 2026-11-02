package com.alumni.portal.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class ConnectionRequestCreateRequest {

    @NotNull(message = "Recipient id is required")
    private Long recipientId;
}
