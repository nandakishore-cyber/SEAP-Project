package com.alumni.portal.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlumniEventRequest {

    @NotBlank
    @Size(max = 150)
    private String title;

    @NotBlank
    @Size(max = 2000)
    private String description;

    @NotNull
    @Future
    private LocalDateTime eventDate;

    @NotBlank
    @Size(max = 150)
    private String location;

    @NotBlank
    @Size(max = 30)
    private String mode;

    @NotNull
    @Min(1)
    private Integer capacity;

    @NotNull
    private LocalDateTime registrationDeadline;
}
