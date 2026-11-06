package com.alumni.portal.dto.response;
import java.time.LocalDateTime;
public record SuccessStoryResponse(Long id, Long authorId, String authorName, String title,
        String content, String achievement, String status, LocalDateTime createdAt) {}
