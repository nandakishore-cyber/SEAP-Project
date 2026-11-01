package com.alumni.portal.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlumniEventResponse {
    private Long id;
    private String title;
    private String description;
    private LocalDateTime eventDate;
    private String location;
    private String mode;
    private Integer capacity;
    private Long registeredCount;
    private LocalDateTime registrationDeadline;
    private String organizerName;
    private Long organizerId;
    private boolean registered;
}
