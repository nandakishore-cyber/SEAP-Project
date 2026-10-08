package com.alumni.portal.integration.controller;

import com.alumni.portal.dto.request.AlumniProfileRequest;
import com.alumni.portal.dto.request.ProfileCreateRequest;
import com.alumni.portal.dto.request.RegisterRequest;
import com.alumni.portal.dto.request.VerificationRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for the full user journey:
 * Register → Create Profile → Create Alumni Profile → View → Admin Verify
 *
 * This end-to-end test validates all features work together.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Full User Journey Integration Tests")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class FullJourneyIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    private static String alumniToken;
    private static String adminToken;
    private static Long alumniProfileId;

    @Test
    @Order(1)
    @DisplayName("Step 1: Register an alumni user")
    void step1_registerAlumniUser() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .firstName("Priya")
                .lastName("Sharma")
                .email("priya@alumni.edu")
                .password("Alumni2024!")
                .role("ROLE_ALUMNI")
                .build();

        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.role").value("ROLE_ALUMNI"))
                .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        alumniToken = json.get("data").get("accessToken").asText();
    }

    @Test
    @Order(2)
    @DisplayName("Step 2: Register an admin user")
    void step2_registerAdminUser() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .firstName("Admin")
                .lastName("User")
                .email("admin@college.edu")
                .password("Admin2024!")
                .role("ROLE_ADMIN")
                .build();

        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.role").value("ROLE_ADMIN"))
                .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        adminToken = json.get("data").get("accessToken").asText();
    }

    @Test
    @Order(3)
    @DisplayName("Step 3: Create basic profile (STLV4-79)")
    void step3_createBasicProfile() throws Exception {
        ProfileCreateRequest request = ProfileCreateRequest.builder()
                .phone("+91-9876543210")
                .dateOfBirth(LocalDate.of(1998, 3, 15))
                .city("Mumbai")
                .state("Maharashtra")
                .country("India")
                .bio("Software engineer and passionate coder")
                .linkedinUrl("https://linkedin.com/in/priyasharma")
                .build();

        mockMvc.perform(post("/api/profiles")
                        .header("Authorization", "Bearer " + alumniToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.city").value("Mumbai"))
                .andExpect(jsonPath("$.data.firstName").value("Priya"));
    }

    @Test
    @Order(4)
    @DisplayName("Step 4: Get own basic profile")
    void step4_getOwnProfile() throws Exception {
        mockMvc.perform(get("/api/profiles/me")
                        .header("Authorization", "Bearer " + alumniToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value("priya@alumni.edu"))
                .andExpect(jsonPath("$.data.city").value("Mumbai"));
    }

    @Test
    @Order(5)
    @DisplayName("Step 5: Create alumni profile (STLV4-61)")
    void step5_createAlumniProfile() throws Exception {
        AlumniProfileRequest request = AlumniProfileRequest.builder()
                .collegeName("IIT Bombay")
                .department("Computer Science")
                .degree("B.Tech")
                .graduationYear(2020)
                .rollNumber("CS2020042")
                .currentCompany("Microsoft")
                .currentDesignation("Senior SDE")
                .industry("Technology")
                .yearsOfExperience(4)
                .skills("Java, Spring Boot, Azure, Microservices")
                .build();

        MvcResult result = mockMvc.perform(post("/api/alumni")
                        .header("Authorization", "Bearer " + alumniToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.collegeName").value("IIT Bombay"))
                .andExpect(jsonPath("$.data.verificationStatus").value("PENDING"))
                .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        alumniProfileId = json.get("data").get("id").asLong();
    }

    @Test
    @Order(6)
    @DisplayName("Step 6: View alumni profile by ID (STLV4-17)")
    void step6_viewAlumniProfile() throws Exception {
        mockMvc.perform(get("/api/alumni/" + alumniProfileId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.firstName").value("Priya"))
                .andExpect(jsonPath("$.data.collegeName").value("IIT Bombay"))
                .andExpect(jsonPath("$.data.currentCompany").value("Microsoft"));
    }

    @Test
    @Order(7)
    @DisplayName("Step 7: Admin views pending verifications")
    void step7_adminViewsPendingVerifications() throws Exception {
        mockMvc.perform(get("/api/alumni/pending")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    @Order(8)
    @DisplayName("Step 8: Admin verifies alumni profile (STLV4-24)")
    void step8_adminVerifiesProfile() throws Exception {
        VerificationRequest request = VerificationRequest.builder()
                .approved(true)
                .remarks("All documents verified. Welcome to the alumni network!")
                .build();

        mockMvc.perform(put("/api/alumni/" + alumniProfileId + "/verify")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.verificationStatus").value("VERIFIED"))
                .andExpect(jsonPath("$.data.verificationRemarks").value("All documents verified. Welcome to the alumni network!"));
    }

    @Test
    @Order(9)
    @DisplayName("Step 9: Search verified alumni")
    void step9_searchVerifiedAlumni() throws Exception {
        mockMvc.perform(get("/api/alumni")
                        .param("department", "Computer Science"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @Order(10)
    @DisplayName("Step 10: Non-admin cannot verify profiles")
    void step10_nonAdminCannotVerify() throws Exception {
        VerificationRequest request = VerificationRequest.builder()
                .approved(true)
                .remarks("Hacking attempt")
                .build();

        mockMvc.perform(put("/api/alumni/" + alumniProfileId + "/verify")
                        .header("Authorization", "Bearer " + alumniToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @Order(11)
    @DisplayName("Step 11: Unauthenticated user cannot create profile")
    void step11_unauthenticatedCannotCreateProfile() throws Exception {
        ProfileCreateRequest request = ProfileCreateRequest.builder()
                .phone("1234567890")
                .build();

        mockMvc.perform(post("/api/profiles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }
}
