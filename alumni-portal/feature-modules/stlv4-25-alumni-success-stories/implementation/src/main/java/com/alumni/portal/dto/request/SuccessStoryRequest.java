package com.alumni.portal.dto.request;
import jakarta.validation.constraints.*;
import lombok.Getter; import lombok.Setter;
@Getter @Setter
public class SuccessStoryRequest {
    @NotBlank @Size(max = 180) private String title;
    @NotBlank @Size(max = 3000) private String content;
    @Size(max = 120) private String achievement;
}
