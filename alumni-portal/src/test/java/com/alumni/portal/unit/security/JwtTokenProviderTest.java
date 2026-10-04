package com.alumni.portal.unit.security;

import com.alumni.portal.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for JwtTokenProvider.
 * Tests are isolated — no Spring context needed.
 */
@DisplayName("JwtTokenProvider Unit Tests")
class JwtTokenProviderTest {

    private JwtTokenProvider tokenProvider;

    // Valid Base64-encoded secret (at least 256 bits for HS256)
    private static final String TEST_SECRET =
            "dGVzdC1zZWNyZXQta2V5LWZvci1qdW5pdC10ZXN0aW5nLW9ubHktZG8tbm90LXVzZS1pbi1wcm9k";
    private static final long EXPIRATION_MS = 3600000;       // 1 hour
    private static final long REFRESH_EXPIRATION_MS = 7200000; // 2 hours

    @BeforeEach
    void setUp() {
        tokenProvider = new JwtTokenProvider(TEST_SECRET, EXPIRATION_MS, REFRESH_EXPIRATION_MS);
    }

    private Authentication createAuthentication(String email) {
        UserDetails userDetails = new User(email, "password",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_ALUMNI")));
        return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }

    @Nested
    @DisplayName("Token Generation")
    class TokenGeneration {

        @Test
        @DisplayName("Should generate a non-null token from Authentication")
        void generateToken_withValidAuthentication_returnsToken() {
            Authentication auth = createAuthentication("test@example.com");

            String token = tokenProvider.generateToken(auth);

            assertThat(token).isNotNull().isNotBlank();
        }

        @Test
        @DisplayName("Should generate a non-null token from username")
        void generateTokenFromUsername_returnsToken() {
            String token = tokenProvider.generateTokenFromUsername("test@example.com");

            assertThat(token).isNotNull().isNotBlank();
        }

        @Test
        @DisplayName("Should generate a refresh token")
        void generateRefreshToken_returnsToken() {
            String token = tokenProvider.generateRefreshToken("test@example.com");

            assertThat(token).isNotNull().isNotBlank();
        }

        @Test
        @DisplayName("Different calls should produce different tokens")
        void generateToken_differentCalls_produceDifferentTokens() {
            String token1 = tokenProvider.generateTokenFromUsername("user1@test.com");
            String token2 = tokenProvider.generateTokenFromUsername("user2@test.com");

            assertThat(token1).isNotEqualTo(token2);
        }
    }

    @Nested
    @DisplayName("Token Parsing")
    class TokenParsing {

        @Test
        @DisplayName("Should extract correct username from token")
        void getUsernameFromToken_returnsCorrectUsername() {
            String email = "alumni@college.edu";
            String token = tokenProvider.generateTokenFromUsername(email);

            String extractedUsername = tokenProvider.getUsernameFromToken(token);

            assertThat(extractedUsername).isEqualTo(email);
        }
    }

    @Nested
    @DisplayName("Token Validation")
    class TokenValidation {

        @Test
        @DisplayName("Should validate a correctly signed token")
        void validateToken_withValidToken_returnsTrue() {
            String token = tokenProvider.generateTokenFromUsername("test@example.com");

            boolean isValid = tokenProvider.validateToken(token);

            assertThat(isValid).isTrue();
        }

        @Test
        @DisplayName("Should reject a tampered token")
        void validateToken_withTamperedToken_returnsFalse() {
            String token = tokenProvider.generateTokenFromUsername("test@example.com");
            String tampered = token + "tampered";

            boolean isValid = tokenProvider.validateToken(tampered);

            assertThat(isValid).isFalse();
        }

        @Test
        @DisplayName("Should reject an empty token")
        void validateToken_withEmptyToken_returnsFalse() {
            boolean isValid = tokenProvider.validateToken("");

            assertThat(isValid).isFalse();
        }

        @Test
        @DisplayName("Should reject a malformed token")
        void validateToken_withMalformedToken_returnsFalse() {
            boolean isValid = tokenProvider.validateToken("not.a.jwt.token");

            assertThat(isValid).isFalse();
        }

        @Test
        @DisplayName("Should reject an expired token")
        void validateToken_withExpiredToken_returnsFalse() {
            // Create provider with 0ms expiry
            JwtTokenProvider shortLivedProvider = new JwtTokenProvider(TEST_SECRET, 0, 0);
            String token = shortLivedProvider.generateTokenFromUsername("test@example.com");

            boolean isValid = shortLivedProvider.validateToken(token);

            assertThat(isValid).isFalse();
        }
    }
}
