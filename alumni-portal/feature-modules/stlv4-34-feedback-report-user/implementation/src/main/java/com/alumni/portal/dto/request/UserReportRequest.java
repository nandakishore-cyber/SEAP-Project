package com.alumni.portal.dto.request;
import jakarta.validation.constraints.*;
import lombok.Getter; import lombok.Setter;
@Getter @Setter
public class UserReportRequest {
    private Long reportedUserId;
    @NotBlank @Size(max = 50) private String category;
    @NotBlank @Size(max = 2000) private String details;
}
