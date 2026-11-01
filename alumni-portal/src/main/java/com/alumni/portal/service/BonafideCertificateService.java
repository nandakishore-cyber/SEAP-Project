package com.alumni.portal.service;

import com.alumni.portal.dto.response.BonafideCertificateResponse;
import com.alumni.portal.entity.BonafideCertificate;
import com.alumni.portal.entity.User;
import com.alumni.portal.exception.ResourceNotFoundException;
import com.alumni.portal.repository.BonafideCertificateRepository;
import com.alumni.portal.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.UUID;

@Service
public class BonafideCertificateService {

    private static final Set<String> ALLOWED_TYPES = Set.of(
            "application/pdf", "image/jpeg", "image/png");

    private final BonafideCertificateRepository certificateRepository;
    private final UserRepository userRepository;
    private final Path uploadDirectory;

    public BonafideCertificateService(BonafideCertificateRepository certificateRepository,
                                      UserRepository userRepository,
                                      @Value("${app.uploads.bonafide-dir:uploads/bonafide}") String uploadDirectory) {
        this.certificateRepository = certificateRepository;
        this.userRepository = userRepository;
        this.uploadDirectory = Paths.get(uploadDirectory).toAbsolutePath().normalize();
    }

    @Transactional
    public BonafideCertificateResponse upload(Long userId, MultipartFile file) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        if (user.getRole().name().equals("ROLE_STUDENT")) {
            throw new IllegalArgumentException("Only alumni can upload a bonafide certificate");
        }
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("A bonafide certificate file is required");
        }
        if (!ALLOWED_TYPES.contains(file.getContentType())) {
            throw new IllegalArgumentException("Only PDF, JPG, and PNG certificates are supported");
        }
        try {
            Files.createDirectories(uploadDirectory);
            String storedFileName = UUID.randomUUID() + "-" + safeFileName(file.getOriginalFilename());
            Files.copy(file.getInputStream(), uploadDirectory.resolve(storedFileName));
            BonafideCertificate certificate = certificateRepository.findByUserId(userId)
                    .orElse(BonafideCertificate.builder().user(user).build());
            certificate.setOriginalFileName(safeFileName(file.getOriginalFilename()));
            certificate.setStoredFileName(storedFileName);
            certificate.setContentType(file.getContentType());
            certificate.setFileSize(file.getSize());
            return map(certificateRepository.save(certificate));
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to store the bonafide certificate", exception);
        }
    }

    @Transactional(readOnly = true)
    public BonafideCertificateResponse getStatus(Long userId) {
        return certificateRepository.findByUserId(userId).map(this::map).orElse(null);
    }

    private String safeFileName(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            return "bonafide-certificate";
        }
        return Paths.get(fileName).getFileName().toString().replaceAll("[^a-zA-Z0-9._-]", "_");
    }

    private BonafideCertificateResponse map(BonafideCertificate certificate) {
        return BonafideCertificateResponse.builder()
                .id(certificate.getId())
                .originalFileName(certificate.getOriginalFileName())
                .contentType(certificate.getContentType())
                .fileSize(certificate.getFileSize())
                .uploadedAt(certificate.getUploadedAt())
                .build();
    }
}
