package com.alumni.portal.dto.request;
import jakarta.validation.constraints.*;
import lombok.Getter; import lombok.Setter;
@Getter @Setter
public class ProfessionalPortfolioRequest {
    @NotBlank @Size(max = 150) private String headline;
    @Size(max = 3000) private String summary;
    @Size(max = 3000) private String experience;
    @Size(max = 3000) private String projects;
    @Size(max = 2000) private String certifications;
    @Size(max = 500) private String websiteUrl;
}
