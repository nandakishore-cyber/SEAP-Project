package com.alumni.portal.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationResponse {

    private Long id;
    private String type;
    private String title;
    private String message;
    private Long relatedId;
    private boolean read;
    private LocalDateTime createdAt;
}
