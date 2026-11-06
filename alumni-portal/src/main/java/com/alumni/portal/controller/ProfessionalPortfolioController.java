package com.alumni.portal.controller;
import com.alumni.portal.dto.request.ProfessionalPortfolioRequest;
import com.alumni.portal.dto.response.*;
import com.alumni.portal.entity.User;
import com.alumni.portal.exception.ResourceNotFoundException;
import com.alumni.portal.repository.UserRepository;
import com.alumni.portal.service.ProfessionalPortfolioService;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/portfolio")
public class ProfessionalPortfolioController {
    private final ProfessionalPortfolioService service; private final UserRepository users;
    public ProfessionalPortfolioController(ProfessionalPortfolioService service,UserRepository users) { this.service=service; this.users=users; }
    @GetMapping("/me") @PreAuthorize("hasRole('ALUMNI')") public ResponseEntity<ApiResponse<ProfessionalPortfolioResponse>> mine(@AuthenticationPrincipal UserDetails p) { return ResponseEntity.ok(ApiResponse.success("Portfolio retrieved",service.get(userId(p)))); }
    @GetMapping("/{userId}") public ResponseEntity<ApiResponse<ProfessionalPortfolioResponse>> get(@PathVariable Long userId) { return ResponseEntity.ok(ApiResponse.success("Portfolio retrieved",service.get(userId))); }
    @PutMapping("/me") @PreAuthorize("hasRole('ALUMNI')") public ResponseEntity<ApiResponse<ProfessionalPortfolioResponse>> save(@AuthenticationPrincipal UserDetails p,@Valid @RequestBody ProfessionalPortfolioRequest request) { return ResponseEntity.ok(ApiResponse.success("Portfolio saved",service.save(userId(p),request))); }
    private Long userId(UserDetails p) { User u=users.findByEmail(p.getUsername()).orElseThrow(() -> new ResourceNotFoundException("User","email",p.getUsername())); return u.getId(); }
}
