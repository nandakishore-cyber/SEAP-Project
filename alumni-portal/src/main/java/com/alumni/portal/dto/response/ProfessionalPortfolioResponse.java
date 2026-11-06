package com.alumni.portal.dto.response;
import java.time.LocalDateTime;
public record ProfessionalPortfolioResponse(Long id, Long userId, String name, String headline,
        String summary, String experience, String projects, String certifications, String websiteUrl,
        LocalDateTime updatedAt) {}
