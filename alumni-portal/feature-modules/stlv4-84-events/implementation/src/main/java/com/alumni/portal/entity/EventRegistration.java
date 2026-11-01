package com.alumni.portal.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "event_registrations",
        uniqueConstraints = @UniqueConstraint(columnNames = {"event_id", "student_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventRegistration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "event_id", nullable = false)
    private AlumniEvent event;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @Column(nullable = false, length = 30)
    private String attendanceMode;

    @Column(length = 100)
    private String currentSemester;

    @Column(length = 500)
    private String expectations;

    @Column(length = 100)
    private String dietaryPreference;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime registeredAt;
}
