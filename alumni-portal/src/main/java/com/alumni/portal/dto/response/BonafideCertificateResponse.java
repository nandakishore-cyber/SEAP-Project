package com.alumni.portal.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BonafideCertificateResponse {

    private Long id;
    private String originalFileName;
    private String contentType;
    private long fileSize;
    private LocalDateTime uploadedAt;
}
