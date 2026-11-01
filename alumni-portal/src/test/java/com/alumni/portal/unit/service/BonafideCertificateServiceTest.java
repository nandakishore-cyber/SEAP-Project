package com.alumni.portal.unit.service;

import com.alumni.portal.entity.User;
import com.alumni.portal.entity.enums.Role;
import com.alumni.portal.repository.BonafideCertificateRepository;
import com.alumni.portal.repository.UserRepository;
import com.alumni.portal.service.BonafideCertificateService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BonafideCertificateServiceTest {

    @Mock
    private BonafideCertificateRepository certificateRepository;

    @Mock
    private UserRepository userRepository;

    private BonafideCertificateService certificateService;

    @BeforeEach
    void setUp() {
        certificateService = new BonafideCertificateService(
                certificateRepository, userRepository, "target/test-bonafide");
    }

    @Test
    void upload_rejectsStudentUpload() {
        User student = User.builder().id(1L).role(Role.ROLE_STUDENT).build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(student));
        MockMultipartFile file = new MockMultipartFile(
                "file", "certificate.pdf", "application/pdf", "certificate".getBytes());

        assertThatThrownBy(() -> certificateService.upload(1L, file))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Only alumni");
    }
}
