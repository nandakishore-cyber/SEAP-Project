package com.alumni.portal.integration.controller;

import com.alumni.portal.dto.request.LoginRequest;
import com.alumni.portal.dto.request.RegisterRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for AuthController.
 * Uses @SpringBootTest with full context + H2 database.
 * Tests the entire request-response flow: Controller → Service → Repository → DB.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Auth Controller Integration Tests")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AuthControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @Test
    @Order(1)
    @DisplayName("POST /api/auth/register — should register successfully")
    void register_withValidRequest_returns201() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .firstName("Integration")
                .lastName("Test")
                .email("integration@test.com")
                .password("TestPass123")
                .build();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.data.email").value("integration@test.com"))
                .andExpect(jsonPath("$.data.firstName").value("Integration"))
                .andExpect(jsonPath("$.data.role").value("ROLE_ALUMNI"));
    }

    @Test
    @Order(2)
    @DisplayName("POST /api/auth/register — should register a student with student role")
    void register_studentRole_returnsStudentResponse() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .firstName("Student")
                .lastName("Test")
                .email("student@test.com")
                .password("StudentPass123")
                .role("ROLE_STUDENT")
                .build();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.role").value("ROLE_STUDENT"));
    }

    @Test
    @Order(3)
    @DisplayName("POST /api/auth/register — should reject duplicate email")
    void register_withDuplicateEmail_returns409() throws Exception {
        // First registration
        RegisterRequest request = RegisterRequest.builder()
                .firstName("Dup")
                .lastName("User")
                .email("duplicate@test.com")
                .password("TestPass123")
                .build();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        // Duplicate registration
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @Order(4)
    @DisplayName("POST /api/auth/register — should reject invalid email format")
    void register_withInvalidEmail_returns400() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .firstName("Bad")
                .lastName("Email")
                .email("not-an-email")
                .password("TestPass123")
                .build();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data.email").isNotEmpty());
    }

    @Test
    @Order(5)
    @DisplayName("POST /api/auth/register — should reject short password")
    void register_withShortPassword_returns400() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .firstName("Short")
                .lastName("Pass")
                .email("short.pass@test.com")
                .password("123")
                .build();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.data.password").isNotEmpty());
    }

    @Test
    @Order(6)
    @DisplayName("POST /api/auth/login — should login successfully")
    void login_withValidCredentials_returns200() throws Exception {
        // Register first
        RegisterRequest regRequest = RegisterRequest.builder()
                .firstName("Login")
                .lastName("Test")
                .email("login@test.com")
                .password("LoginPass123")
                .build();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(regRequest)))
                .andExpect(status().isCreated());

        // Login
        LoginRequest loginRequest = LoginRequest.builder()
                .email("login@test.com")
                .password("LoginPass123")
                .build();

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"));
    }

    @Test
    @Order(7)
    @DisplayName("POST /api/auth/login — should reject a login from the wrong role page")
    void login_withWrongRole_returns401() throws Exception {
        RegisterRequest registerRequest = RegisterRequest.builder()
                .firstName("Role")
                .lastName("Test")
                .email("role.test@test.com")
                .password("RolePass123")
                .role("ROLE_STUDENT")
                .build();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());

        LoginRequest loginRequest = LoginRequest.builder()
                .email("role.test@test.com")
                .password("RolePass123")
                .role("ROLE_ALUMNI")
                .build();

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Order(8)
    @DisplayName("POST /api/auth/login — should reject wrong password")
    void login_withWrongPassword_returns401() throws Exception {
        // Register
        RegisterRequest regRequest = RegisterRequest.builder()
                .firstName("Wrong")
                .lastName("Pass")
                .email("wrongpass@test.com")
                .password("CorrectPass123")
                .build();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(regRequest)))
                .andExpect(status().isCreated());

        // Login with wrong password
        LoginRequest loginRequest = LoginRequest.builder()
                .email("wrongpass@test.com")
                .password("WrongPassword123")
                .build();

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }
}
