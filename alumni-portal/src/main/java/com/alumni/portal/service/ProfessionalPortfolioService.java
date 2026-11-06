package com.alumni.portal.service;

import com.alumni.portal.dto.request.ProfessionalPortfolioRequest;
import com.alumni.portal.dto.response.ProfessionalPortfolioResponse;
import com.alumni.portal.entity.*;
import com.alumni.portal.entity.enums.Role;
import com.alumni.portal.exception.ResourceNotFoundException;
import com.alumni.portal.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProfessionalPortfolioService {
    private final ProfessionalPortfolioRepository portfolios; private final UserRepository users;
    public ProfessionalPortfolioService(ProfessionalPortfolioRepository portfolios, UserRepository users) { this.portfolios=portfolios; this.users=users; }
    @Transactional(readOnly = true) public ProfessionalPortfolioResponse get(Long userId) {
        return portfolios.findByUserId(userId).map(this::map).orElseThrow(() -> new ResourceNotFoundException("ProfessionalPortfolio", "userId", userId));
    }
    @Transactional public ProfessionalPortfolioResponse save(Long userId, ProfessionalPortfolioRequest request) {
        User user=users.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        if (user.getRole()!=Role.ROLE_ALUMNI) throw new IllegalArgumentException("Only alumni can manage a professional portfolio");
        String websiteUrl = request.getWebsiteUrl() == null ? null : request.getWebsiteUrl().trim();
        if (websiteUrl != null && !websiteUrl.isBlank()
                && !websiteUrl.matches("(?i)^https?://.+")) {
            throw new IllegalArgumentException("Portfolio website must use an http or https URL");
        }
        ProfessionalPortfolio p=portfolios.findByUserId(userId).orElseGet(() -> ProfessionalPortfolio.builder().user(user).build());
        p.setHeadline(request.getHeadline().trim()); p.setSummary(request.getSummary()); p.setExperience(request.getExperience());
        p.setProjects(request.getProjects()); p.setCertifications(request.getCertifications()); p.setWebsiteUrl(websiteUrl);
        return map(portfolios.save(p));
    }
    private ProfessionalPortfolioResponse map(ProfessionalPortfolio p) { User u=p.getUser(); return new ProfessionalPortfolioResponse(p.getId(),u.getId(),u.getFirstName()+" "+u.getLastName(),p.getHeadline(),p.getSummary(),p.getExperience(),p.getProjects(),p.getCertifications(),p.getWebsiteUrl(),p.getUpdatedAt()); }
}
