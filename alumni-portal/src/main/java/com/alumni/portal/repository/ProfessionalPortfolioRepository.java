package com.alumni.portal.repository;
import com.alumni.portal.entity.ProfessionalPortfolio;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface ProfessionalPortfolioRepository extends JpaRepository<ProfessionalPortfolio, Long> {
    Optional<ProfessionalPortfolio> findByUserId(Long userId);
}
