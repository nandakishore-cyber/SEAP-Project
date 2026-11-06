package com.alumni.portal.service;

import com.alumni.portal.dto.request.UserReportRequest;
import com.alumni.portal.dto.response.UserReportResponse;
import com.alumni.portal.entity.*;
import com.alumni.portal.exception.ResourceNotFoundException;
import com.alumni.portal.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class UserReportService {
    private final UserReportRepository reports; private final UserRepository users;
    public UserReportService(UserReportRepository reports, UserRepository users) { this.reports = reports; this.users = users; }
    @Transactional public UserReportResponse submit(Long reporterId, UserReportRequest request) {
        User reporter = user(reporterId);
        User target = request.getReportedUserId() == null ? null : user(request.getReportedUserId());
        if (target == null && !"FEEDBACK".equalsIgnoreCase(request.getCategory()))
            throw new IllegalArgumentException("Choose a user to report, or select general feedback");
        if (reporterId.equals(target == null ? null : target.getId())) throw new IllegalArgumentException("You cannot report your own account");
        UserReport report = UserReport.builder().reporter(reporter).reportedUser(target)
                .category(request.getCategory().trim()).details(request.getDetails().trim()).status("OPEN").build();
        return map(reports.save(report));
    }
    @Transactional(readOnly = true) public List<UserReportResponse> mine(Long userId) {
        return reports.findByReporterIdOrderByCreatedAtDesc(userId).stream().map(this::map).toList();
    }
    @Transactional(readOnly = true) public List<UserReportResponse> all() {
        return reports.findAllByOrderByCreatedAtDesc().stream().map(this::map).toList();
    }
    @Transactional public UserReportResponse updateStatus(Long id, String status) {
        if (!List.of("OPEN", "REVIEWING", "RESOLVED", "DISMISSED").contains(status)) throw new IllegalArgumentException("Invalid report status");
        UserReport report = reports.findById(id).orElseThrow(() -> new ResourceNotFoundException("UserReport", "id", id));
        report.setStatus(status); return map(reports.save(report));
    }
    private User user(Long id) { return users.findById(id).orElseThrow(() -> new ResourceNotFoundException("User", "id", id)); }
    private UserReportResponse map(UserReport r) {
        User target = r.getReportedUser();
        return new UserReportResponse(r.getId(), target == null ? null : target.getId(),
                target == null ? null : target.getFirstName()+" "+target.getLastName(), r.getCategory(),
                r.getDetails(), r.getStatus(), r.getCreatedAt());
    }
}
