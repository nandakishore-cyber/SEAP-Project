package com.alumni.portal.dto.response;
import java.time.LocalDateTime;
public record UserReportResponse(Long id, Long reporterId, String reporterName, Long reportedUserId,
                                 String reportedUserName, String category,
                                 String details, String status, LocalDateTime createdAt) {}
