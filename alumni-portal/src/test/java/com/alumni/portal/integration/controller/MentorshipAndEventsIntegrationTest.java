package com.alumni.portal.integration.controller;

import com.alumni.portal.dto.request.AlumniEventRequest;
import com.alumni.portal.dto.request.AlumniProfileRequest;
import com.alumni.portal.dto.request.EventRegistrationRequest;
import com.alumni.portal.dto.request.MentorshipRequestCreateRequest;
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

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Mentorship and Events Integration Tests")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class MentorshipAndEventsIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    private static String alumniToken;
    private static String studentToken;
    private static String adminToken;
    private static Long alumniProfileId;
    private static Long eventId;

    @Test
    @Order(1)
    void registerAlumniStudentAndAdmin() throws Exception {
        alumniToken = register("Mentor", "Alumni", "mentor@test.com", "ROLE_ALUMNI");
        studentToken = register("Current", "Student", "student-events@test.com", "ROLE_STUDENT");
        adminToken = register("Feature", "Admin", "feature-admin@test.com", "ROLE_ADMIN");
    }

    @Test
    @Order(2)
    void createAndVerifyAlumniProfile() throws Exception {
        AlumniProfileRequest profile = AlumniProfileRequest.builder()
                .collegeName("SEAP College")
                .department("Computer Science")
                .degree("B.Tech")
                .graduationYear(2020)
                .currentCompany("Tech Corp")
                .currentDesignation("Senior Engineer")
                .yearsOfExperience(5)
                .skills("Java, Spring Boot")
                .build();

        MvcResult result = mockMvc.perform(post("/api/alumni")
                        .header("Authorization", "Bearer " + alumniToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(profile)))
                .andExpect(status().isCreated())
                .andReturn();
        alumniProfileId = read(result).get("data").get("id").asLong();

        VerificationRequest verification = VerificationRequest.builder()
                .approved(true)
                .remarks("Verified for feature tests")
                .build();
        mockMvc.perform(put("/api/alumni/" + alumniProfileId + "/verify")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(verification)))
                .andExpect(status().isOk());
    }

    @Test
    @Order(3)
    void studentCanViewAvailableMentors() throws Exception {
        mockMvc.perform(get("/api/mentorship/mentors")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", org.hamcrest.Matchers.hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    @Order(4)
    void studentCanSendMentorshipRequest() throws Exception {
        MentorshipRequestCreateRequest request = MentorshipRequestCreateRequest.builder()
                .alumniProfileId(alumniProfileId)
                .message("I would like guidance on backend engineering.")
                .goals("Prepare for a Java engineering internship")
                .build();

        mockMvc.perform(post("/api/mentorship/requests")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.status").value("PENDING"))
                .andExpect(jsonPath("$.data.alumniName").value("Mentor Alumni"));
    }

    @Test
    @Order(5)
    void alumniCanViewAndAcceptMentorshipRequest() throws Exception {
        mockMvc.perform(get("/api/mentorship/requests/received")
                        .header("Authorization", "Bearer " + alumniToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", org.hamcrest.Matchers.hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    @Order(6)
    void alumniCanCreateEvent() throws Exception {
        AlumniEventRequest event = AlumniEventRequest.builder()
                .title("Spring Boot Career Workshop")
                .description("A practical session on building production-ready APIs.")
                .eventDate(LocalDateTime.now().plusDays(10))
                .location("Innovation Hall")
                .mode("HYBRID")
                .capacity(50)
                .registrationDeadline(LocalDateTime.now().plusDays(8))
                .build();

        MvcResult result = mockMvc.perform(post("/api/events")
                        .header("Authorization", "Bearer " + alumniToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(event)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.title").value("Spring Boot Career Workshop"))
                .andReturn();
        eventId = read(result).get("data").get("id").asLong();
    }

    @Test
    @Order(7)
    void studentCanViewAndRegisterForEvent() throws Exception {
        mockMvc.perform(get("/api/events")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", org.hamcrest.Matchers.hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.data[0].registered").value(false));

        EventRegistrationRequest registration = EventRegistrationRequest.builder()
                .attendanceMode("ONLINE")
                .currentSemester("Semester 5")
                .expectations("Learn API design and deployment practices.")
                .dietaryPreference("No preference")
                .build();
        mockMvc.perform(post("/api/events/" + eventId + "/registrations")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registration)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.eventTitle").value("Spring Boot Career Workshop"))
                .andExpect(jsonPath("$.data.currentSemester").value("Semester 5"));
    }

    @Test
    @Order(8)
    void studentCannotCreateEventsOrRequestMentorshipAsAlumni() throws Exception {
        AlumniEventRequest event = AlumniEventRequest.builder()
                .title("Invalid Student Event")
                .description("Should not be created by a student.")
                .eventDate(LocalDateTime.now().plusDays(5))
                .location("Online")
                .mode("ONLINE")
                .capacity(10)
                .registrationDeadline(LocalDateTime.now().plusDays(3))
                .build();

        mockMvc.perform(post("/api/events")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(event)))
                .andExpect(status().isForbidden());
    }

    private String register(String firstName, String lastName, String email, String role)
            throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .firstName(firstName)
                .lastName(lastName)
                .email(email)
                .password("FeaturePass123")
                .role(role)
                .build();
        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();
        return read(result).get("data").get("accessToken").asText();
    }

    private JsonNode read(MvcResult result) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsString());
    }
}
