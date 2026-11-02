package com.alumni.portal.integration.controller;

import com.alumni.portal.entity.ConnectionRequest;
import com.alumni.portal.entity.User;
import com.alumni.portal.entity.enums.ConnectionStatus;
import com.alumni.portal.entity.enums.Role;
import com.alumni.portal.repository.ConnectionRequestRepository;
import com.alumni.portal.repository.UserRepository;
import com.alumni.portal.security.JwtTokenProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ConnectionRequestControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private UserRepository userRepository;
    @Autowired private ConnectionRequestRepository connectionRequestRepository;
    @Autowired private com.alumni.portal.repository.NotificationRepository notificationRepository;
    @Autowired private JwtTokenProvider jwtTokenProvider;

    @AfterEach
    void tearDown() {
        notificationRepository.deleteAll();
        connectionRequestRepository.deleteAll();
        userRepository.deleteAll();
    }

    private User createUser(String firstName, String lastName, String email) {
        User user = User.builder()
                .firstName(firstName)
                .lastName(lastName)
                .email(email)
                .password("encoded-password")
                .role(Role.ROLE_ALUMNI)
                .enabled(true)
                .build();
        return userRepository.save(user);
    }

    private String uniqueEmail(String prefix) {
        return prefix + "-" + UUID.randomUUID() + "@test.com";
    }

    private String bearerTokenFor(User user) {
        return "Bearer " + jwtTokenProvider.generateTokenFromUsername(user.getEmail());
    }

    @Test
    @DisplayName("POST /api/connections should send a connection request successfully")
    void sendRequest_withValidUser_returns201() throws Exception {
        User sender = createUser("Alice", "Brown", "alice@test.com");
        User recipient = createUser("Bob", "White", "bob@test.com");

        mockMvc.perform(post("/api/connections")
                        .header("Authorization", bearerTokenFor(sender))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("recipientId", recipient.getId()))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("PENDING"))
                .andExpect(jsonPath("$.data.senderId").value(sender.getId()))
                .andExpect(jsonPath("$.data.recipientId").value(recipient.getId()));
    }

    @Test
    @DisplayName("POST /api/connections should fail validation when recipientId is missing")
    void sendRequest_withoutRecipientId_returns400() throws Exception {
        User sender = createUser("Alice", "Brown", "alice@test.com");

        mockMvc.perform(post("/api/connections")
                        .header("Authorization", bearerTokenFor(sender))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("POST /api/connections should reject self requests")
    void sendRequest_selfRequest_returns400() throws Exception {
        User sender = createUser("Alice", "Brown", "alice@test.com");

        mockMvc.perform(post("/api/connections")
                        .header("Authorization", bearerTokenFor(sender))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("recipientId", sender.getId()))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("GET /api/connections/sent should return current user's sent requests")
    void getSentRequests_returnsCurrentUserSentRequests() throws Exception {
        User sender = createUser("Alice", "Brown", "alice@test.com");
        User recipient = createUser("Bob", "White", "bob@test.com");
        connectionRequestRepository.save(ConnectionRequest.builder()
                .sender(sender)
                .recipient(recipient)
                .status(ConnectionStatus.PENDING)
                .build());

        mockMvc.perform(get("/api/connections/sent")
                        .header("Authorization", bearerTokenFor(sender)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].senderId").value(sender.getId()));
    }

    @Test
    @DisplayName("GET /api/connections/received should return current user's received requests")
    void getReceivedRequests_returnsCurrentUserReceivedRequests() throws Exception {
        User sender = createUser("Alice", "Brown", "alice@test.com");
        User recipient = createUser("Bob", "White", "bob@test.com");
        connectionRequestRepository.save(ConnectionRequest.builder()
                .sender(sender)
                .recipient(recipient)
                .status(ConnectionStatus.PENDING)
                .build());

        mockMvc.perform(get("/api/connections/received")
                        .header("Authorization", bearerTokenFor(recipient)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].recipientId").value(recipient.getId()));
    }

    @Test
    @DisplayName("PUT /api/connections/{id}/accept should only allow recipient to accept")
    void acceptRequest_bySender_returns403() throws Exception {
        User sender = createUser("Alice", "Brown", "alice@test.com");
        User recipient = createUser("Bob", "White", "bob@test.com");
        ConnectionRequest request = connectionRequestRepository.save(ConnectionRequest.builder()
                .sender(sender)
                .recipient(recipient)
                .status(ConnectionStatus.PENDING)
                .build());

        mockMvc.perform(put("/api/connections/{id}/accept", request.getId())
                        .header("Authorization", bearerTokenFor(sender)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("PUT /api/connections/{id}/accept should accept request for recipient")
    void acceptRequest_asRecipient_returns200() throws Exception {
        User sender = createUser("Alice", "Brown", "alice@test.com");
        User recipient = createUser("Bob", "White", "bob@test.com");
        ConnectionRequest request = connectionRequestRepository.save(ConnectionRequest.builder()
                .sender(sender)
                .recipient(recipient)
                .status(ConnectionStatus.PENDING)
                .build());

        mockMvc.perform(put("/api/connections/{id}/accept", request.getId())
                        .header("Authorization", bearerTokenFor(recipient)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("ACCEPTED"));
    }

    @Test
    @DisplayName("PUT /api/connections/{id}/reject should reject request for recipient")
    void rejectRequest_asRecipient_returns200() throws Exception {
        User sender = createUser("Alice", "Brown", "alice@test.com");
        User recipient = createUser("Bob", "White", "bob@test.com");
        ConnectionRequest request = connectionRequestRepository.save(ConnectionRequest.builder()
                .sender(sender)
                .recipient(recipient)
                .status(ConnectionStatus.PENDING)
                .build());

        mockMvc.perform(put("/api/connections/{id}/reject", request.getId())
                        .header("Authorization", bearerTokenFor(recipient)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("REJECTED"));
    }

    @Test
    @DisplayName("Unauthenticated requests should be rejected")
    void unauthenticatedRequest_returns401() throws Exception {
        mockMvc.perform(get("/api/connections/sent"))
                .andExpect(status().isUnauthorized());
    }
}
