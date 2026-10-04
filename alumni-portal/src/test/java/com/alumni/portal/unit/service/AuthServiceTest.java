package com.alumni.portal.unit.service;

import com.alumni.portal.dto.request.LoginRequest;
import com.alumni.portal.dto.request.RegisterRequest;
import com.alumni.portal.dto.response.AuthResponse;
import com.alumni.portal.entity.User;
import com.alumni.portal.entity.enums.Role;
import com.alumni.portal.exception.DuplicateResourceException;
import com.alumni.portal.repository.UserRepository;
import com.alumni.portal.security.JwtTokenProvider;
import com.alumni.portal.service.AuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AuthService.
 * Uses Mockito to isolate from database and security infrastructure.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Unit Tests")
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private JwtTokenProvider jwtTokenProvider;

    @InjectMocks private AuthService authService;

    // ---- Test Data Builders ----

    private RegisterRequest buildRegisterRequest() {
        return RegisterRequest.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@college.edu")
                .password("SecurePass123")
                .build();
    }

    private User buildUser() {
        return User.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@college.edu")
                .password("encoded-password")
                .role(Role.ROLE_ALUMNI)
                .enabled(true)
                .build();
    }

    @Nested
    @DisplayName("Registration Tests")
    class RegistrationTests {

        @Test
        @DisplayName("Should register a new user successfully")
        void register_withValidRequest_returnsAuthResponse() {
            RegisterRequest request = buildRegisterRequest();
            User savedUser = buildUser();
            Authentication mockAuth = mock(Authentication.class);

            when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
            when(passwordEncoder.encode(request.getPassword())).thenReturn("encoded-password");
            when(userRepository.save(any(User.class))).thenReturn(savedUser);
            when(authenticationManager.authenticate(any())).thenReturn(mockAuth);
            when(jwtTokenProvider.generateToken(mockAuth)).thenReturn("jwt-token-123");

            AuthResponse response = authService.register(request);

            assertThat(response).isNotNull();
            assertThat(response.getAccessToken()).isEqualTo("jwt-token-123");
            assertThat(response.getEmail()).isEqualTo("john.doe@college.edu");
            assertThat(response.getFirstName()).isEqualTo("John");
            assertThat(response.getRole()).isEqualTo("ROLE_ALUMNI");

            verify(userRepository).save(any(User.class));
            verify(passwordEncoder).encode("SecurePass123");
        }

        @Test
        @DisplayName("Should throw DuplicateResourceException for existing email")
        void register_withExistingEmail_throwsDuplicateResourceException() {
            RegisterRequest request = buildRegisterRequest();
            when(userRepository.existsByEmail(request.getEmail())).thenReturn(true);

            assertThatThrownBy(() -> authService.register(request))
                    .isInstanceOf(DuplicateResourceException.class)
                    .hasMessageContaining("john.doe@college.edu");

            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should default role to ROLE_ALUMNI when not specified")
        void register_withNoRole_defaultsToAlumni() {
            RegisterRequest request = buildRegisterRequest();
            request.setRole(null);
            User savedUser = buildUser();
            Authentication mockAuth = mock(Authentication.class);

            when(userRepository.existsByEmail(anyString())).thenReturn(false);
            when(passwordEncoder.encode(anyString())).thenReturn("encoded");
            when(userRepository.save(any(User.class))).thenReturn(savedUser);
            when(authenticationManager.authenticate(any())).thenReturn(mockAuth);
            when(jwtTokenProvider.generateToken(any())).thenReturn("token");

            AuthResponse response = authService.register(request);

            assertThat(response.getRole()).isEqualTo("ROLE_ALUMNI");
        }
    }

    @Nested
    @DisplayName("Login Tests")
    class LoginTests {

        @Test
        @DisplayName("Should login successfully with valid credentials")
        void login_withValidCredentials_returnsAuthResponse() {
            LoginRequest request = LoginRequest.builder()
                    .email("john.doe@college.edu")
                    .password("SecurePass123")
                    .build();
            User user = buildUser();
            Authentication mockAuth = mock(Authentication.class);

            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenReturn(mockAuth);
            when(jwtTokenProvider.generateToken(mockAuth)).thenReturn("jwt-token");
            when(userRepository.findByEmail("john.doe@college.edu")).thenReturn(Optional.of(user));

            AuthResponse response = authService.login(request);

            assertThat(response.getAccessToken()).isEqualTo("jwt-token");
            assertThat(response.getEmail()).isEqualTo("john.doe@college.edu");
            assertThat(response.getUserId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("Should throw BadCredentialsException for invalid password")
        void login_withInvalidPassword_throwsBadCredentials() {
            LoginRequest request = LoginRequest.builder()
                    .email("john.doe@college.edu")
                    .password("WrongPassword")
                    .build();

            when(authenticationManager.authenticate(any()))
                    .thenThrow(new BadCredentialsException("Bad credentials"));

            assertThatThrownBy(() -> authService.login(request))
                    .isInstanceOf(BadCredentialsException.class);
        }
    }
}
