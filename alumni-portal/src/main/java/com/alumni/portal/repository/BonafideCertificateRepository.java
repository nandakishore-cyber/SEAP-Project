package com.alumni.portal.repository;

import com.alumni.portal.entity.BonafideCertificate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BonafideCertificateRepository extends JpaRepository<BonafideCertificate, Long> {

    Optional<BonafideCertificate> findByUserId(Long userId);

    boolean existsByUserId(Long userId);
}
