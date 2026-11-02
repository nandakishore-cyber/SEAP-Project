package com.alumni.portal.integration.controller;

import com.alumni.portal.entity.Notification;
import com.alumni.portal.entity.User;
import com.alumni.portal.entity.enums.NotificationType;
import com.alumni.portal.entity.enums.Role;
import com.alumni.portal.repository.ConnectionRequestRepository;
import com.alumni.portal.repository.NotificationRepository;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ConnectionNotificationIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private UserRepository userRepository;
    @Autowired private ConnectionRequestRepository connectionRequestRepository;
    @Autowired private NotificationRepository notificationRepository;
    @Autowired private JwtTokenProvider jwtTokenProvider;

    @AfterEach
    void tearDown() {
        notificationRepository.deleteAll();
        connectionRequestRepository.deleteAll();
        userRepository.deleteAll();
    }

    private User createUser(String firstName, String lastName, String email) {
        return userRepository.save(User.builder()
                .firstName(firstName)
                .lastName(lastName)
                .email(email)
                .password("encoded-password")
                .role(Role.ROLE_ALUMNI)
                .enabled(true)
                .build());
    }

    private String bearerTokenFor(User user) {
        return "Bearer " + jwtTokenProvider.generateTokenFromUsername(user.getEmail());
    }

    @Test
    @DisplayName("Sending a request creates a recipient notification")
    void sendRequest_createsRecipientNotification() throws Exception {
        User sender = createUser("Alice", "Brown", "alice@test.com");
        User recipient = createUser("Bob", "White", "bob@test.com");

        mockMvc.perform(post("/api/connections")
                        .header("Authorization", bearerTokenFor(sender))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("recipientId", recipient.getId()))))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/notifications")
                        .header("Authorization", bearerTokenFor(recipient)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].type").value("NEW_CONNECTION_REQUEST"));
    }

    @Test
    @DisplayName("Accepting a request creates a sender notification")
    void acceptRequest_createsSenderNotification() throws Exception {
        User sender = createUser("Alice", "Brown", "alice@test.com");
        User recipient = createUser("Bob", "White", "bob@test.com");

        var request = connectionRequestRepository.save(com.alumni.portal.entity.ConnectionRequest.builder()
                .sender(sender)
                .recipient(recipient)
                .status(com.alumni.portal.entity.enums.ConnectionStatus.PENDING)
                .build());

        mockMvc.perform(put("/api/connections/{id}/accept", request.getId())
                        .header("Authorization", bearerTokenFor(recipient)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/notifications")
                        .header("Authorization", bearerTokenFor(sender)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].type").value("CONNECTION_REQUEST_ACCEPTED"));
    }

    @Test
    @DisplayName("Rejecting a request creates a sender notification")
    void rejectRequest_createsSenderNotification() throws Exception {
        User sender = createUser("Alice", "Brown", "alice@test.com");
        User recipient = createUser("Bob", "White", "bob@test.com");

        var request = connectionRequestRepository.save(com.alumni.portal.entity.ConnectionRequest.builder()
                .sender(sender)
                .recipient(recipient)
                .status(com.alumni.portal.entity.enums.ConnectionStatus.PENDING)
                .build());

        mockMvc.perform(put("/api/connections/{id}/reject", request.getId())
                        .header("Authorization", bearerTokenFor(recipient)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/notifications")
                        .header("Authorization", bearerTokenFor(sender)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].type").value("CONNECTION_REQUEST_REJECTED"));
    }

    @Test
    @DisplayName("Users can retrieve only their own notifications")
    void usersCanRetrieveOnlyTheirOwnNotifications() throws Exception {
        User userOne = createUser("Alice", "Brown", "alice@test.com");
        User userTwo = createUser("Bob", "White", "bob@test.com");
        Notification userOneNotification = notificationRepository.save(Notification.builder()
                .user(userOne)
                .type(NotificationType.NEW_CONNECTION_REQUEST)
                .title("New connection request")
                .message("Request from Bob.")
                .read(false)
                .build());
        notificationRepository.save(Notification.builder()
                .user(userTwo)
                .type(NotificationType.NEW_CONNECTION_REQUEST)
                .title("New connection request")
                .message("Request from Alice.")
                .read(false)
                .build());

        mockMvc.perform(get("/api/notifications")
                        .header("Authorization", bearerTokenFor(userOne)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].id").value(userOneNotification.getId()));
    }
}
