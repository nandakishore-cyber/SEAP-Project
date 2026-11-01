package com.alumni.portal.repository;

import com.alumni.portal.entity.EventRegistration;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EventRegistrationRepository extends JpaRepository<EventRegistration, Long> {
    boolean existsByEventIdAndStudentId(Long eventId, Long studentId);
    long countByEventId(Long eventId);
    List<EventRegistration> findByStudentIdOrderByRegisteredAtDesc(Long studentId);
}
