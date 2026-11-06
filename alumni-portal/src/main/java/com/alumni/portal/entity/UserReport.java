package com.alumni.portal.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_reports")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class UserReport {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "reporter_id", nullable = false) private User reporter;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "reported_user_id") private User reportedUser;
    @Column(nullable = false, length = 50) private String category;
    @Column(nullable = false, length = 2000) private String details;
    @Column(nullable = false, length = 20) private String status = "OPEN";
    @CreationTimestamp @Column(updatable = false) private LocalDateTime createdAt;
    @UpdateTimestamp private LocalDateTime updatedAt;
}
