package com.alumni.portal.repository;

import com.alumni.portal.entity.MentorshipRequest;
import com.alumni.portal.entity.enums.MentorshipRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MentorshipRequestRepository extends JpaRepository<MentorshipRequest, Long> {
    boolean existsByStudentIdAndAlumniProfileIdAndStatus(Long studentId, Long alumniProfileId,
                                                          MentorshipRequestStatus status);
    List<MentorshipRequest> findByStudentIdOrderByCreatedAtDesc(Long studentId);
    List<MentorshipRequest> findByAlumniProfileUserIdOrderByCreatedAtDesc(Long alumniUserId);
}
