package com.alumni.portal.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventRegistrationResponse {
    private Long id;
    private Long eventId;
    private String eventTitle;
    private String attendanceMode;
    private String currentSemester;
    private String expectations;
    private String dietaryPreference;
    private LocalDateTime registeredAt;
}
