package com.alumni.portal.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "alumni_events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlumniEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(nullable = false, length = 2000)
    private String description;

    @Column(nullable = false)
    private LocalDateTime eventDate;

    @Column(nullable = false, length = 150)
    private String location;

    @Column(nullable = false, length = 30)
    private String mode;

    @Column(nullable = false)
    private Integer capacity;

    @Column(nullable = false)
    private LocalDateTime registrationDeadline;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organizer_id", nullable = false)
    private User organizer;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
}
