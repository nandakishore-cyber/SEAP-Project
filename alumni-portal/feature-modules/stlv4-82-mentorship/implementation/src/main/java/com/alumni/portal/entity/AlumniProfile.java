package com.alumni.portal.entity;

import com.alumni.portal.entity.enums.VerificationStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Alumni-specific profile with academic & professional details.
 * Supports verification workflow (STLV4-24: Alumni Profile Verification).
 */
@Entity
@Table(name = "alumni_profiles")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class AlumniProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    // ---- Academic Details ----
    @Column(nullable = false, length = 100)
    private String collegeName;

    @Column(nullable = false, length = 100)
    private String department;

    @Column(nullable = false, length = 100)
    private String degree;

    @Column(nullable = false)
    private Integer graduationYear;

    @Column(length = 50)
    private String rollNumber;

    // ---- Professional Details ----
    @Column(length = 150)
    private String currentCompany;

    @Column(length = 100)
    private String currentDesignation;

    @Column(length = 100)
    private String industry;

    @Column(nullable = false)
    private Integer yearsOfExperience = 0;

    @Column(length = 1000)
    private String skills;

    // ---- Verification ----
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private VerificationStatus verificationStatus = VerificationStatus.PENDING;

    @Column(length = 500)
    private String verificationRemarks;

    private LocalDateTime verifiedAt;

    // ---- Audit ----
    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
