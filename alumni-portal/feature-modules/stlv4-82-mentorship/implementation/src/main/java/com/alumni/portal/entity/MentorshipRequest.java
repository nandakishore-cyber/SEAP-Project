package com.alumni.portal.entity;

import com.alumni.portal.entity.enums.MentorshipRequestStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "mentorship_requests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MentorshipRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "alumni_profile_id", nullable = false)
    private AlumniProfile alumniProfile;

    @Column(nullable = false, length = 1000)
    private String message;

    @Column(length = 500)
    private String goals;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MentorshipRequestStatus status = MentorshipRequestStatus.PENDING;

    @Column(length = 1000)
    private String responseMessage;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
