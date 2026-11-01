package com.alumni.portal.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MentorshipRequestResponse {
    private Long id;
    private Long studentId;
    private String studentName;
    private Long alumniProfileId;
    private String alumniName;
    private String alumniCompany;
    private String alumniDesignation;
    private String message;
    private String goals;
    private String status;
    private String responseMessage;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
