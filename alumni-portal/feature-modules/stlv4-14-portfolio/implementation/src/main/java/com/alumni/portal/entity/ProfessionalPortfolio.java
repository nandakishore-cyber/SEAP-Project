package com.alumni.portal.entity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;
@Entity @Table(name = "professional_portfolios")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ProfessionalPortfolio {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @OneToOne(fetch = FetchType.LAZY) @JoinColumn(name = "user_id", nullable = false, unique = true) private User user;
    @Column(nullable = false, length = 150) private String headline;
    @Column(length = 3000) private String summary;
    @Column(length = 3000) private String experience;
    @Column(length = 3000) private String projects;
    @Column(length = 2000) private String certifications;
    @Column(length = 500) private String websiteUrl;
    @CreationTimestamp @Column(updatable = false) private LocalDateTime createdAt;
    @UpdateTimestamp private LocalDateTime updatedAt;
}
