package com.alumni.portal.controller;
import com.alumni.portal.dto.request.UserReportRequest;
import com.alumni.portal.dto.response.*;
import com.alumni.portal.entity.User;
import com.alumni.portal.exception.ResourceNotFoundException;
import com.alumni.portal.repository.UserRepository;
import com.alumni.portal.service.UserReportService;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController @RequestMapping("/api/reports")
public class UserReportController {
    private final UserReportService service; private final UserRepository users;
    public UserReportController(UserReportService service, UserRepository users) { this.service=service; this.users=users; }
    @PostMapping public ResponseEntity<ApiResponse<UserReportResponse>> submit(@AuthenticationPrincipal UserDetails principal, @Valid @RequestBody UserReportRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Report submitted for review", service.submit(userId(principal), request)));
    }
    @GetMapping("/me") public ResponseEntity<ApiResponse<List<UserReportResponse>>> mine(@AuthenticationPrincipal UserDetails principal) { return ResponseEntity.ok(ApiResponse.success("Your reports",service.mine(userId(principal)))); }
    @GetMapping @PreAuthorize("hasRole('ADMIN')") public ResponseEntity<ApiResponse<List<UserReportResponse>>> all() { return ResponseEntity.ok(ApiResponse.success("User reports",service.all())); }
    @PutMapping("/{id}/status") @PreAuthorize("hasRole('ADMIN')") public ResponseEntity<ApiResponse<UserReportResponse>> update(@PathVariable Long id,@RequestParam String status) { return ResponseEntity.ok(ApiResponse.success("Report updated",service.updateStatus(id,status.toUpperCase()))); }
    private Long userId(UserDetails p) { User u=users.findByEmail(p.getUsername()).orElseThrow(() -> new ResourceNotFoundException("User","email",p.getUsername())); return u.getId(); }
}
