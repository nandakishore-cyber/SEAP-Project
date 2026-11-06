package com.alumni.portal.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "success_stories")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SuccessStory {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "author_id", nullable = false) private User author;
    @Column(nullable = false, length = 180) private String title;
    @Column(nullable = false, length = 3000) private String content;
    @Column(length = 120) private String achievement;
    @Column(nullable = false, length = 20) private String status = "PENDING";
    @CreationTimestamp @Column(updatable = false) private LocalDateTime createdAt;
    @UpdateTimestamp private LocalDateTime updatedAt;
}
