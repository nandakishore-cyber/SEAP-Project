package com.alumni.portal.repository;
import com.alumni.portal.entity.UserReport;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface UserReportRepository extends JpaRepository<UserReport, Long> {
    List<UserReport> findByReporterIdOrderByCreatedAtDesc(Long reporterId);
    List<UserReport> findAllByOrderByCreatedAtDesc();
}
