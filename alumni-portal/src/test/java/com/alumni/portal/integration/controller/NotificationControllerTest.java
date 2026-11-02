package com.alumni.portal.integration.controller;

import com.alumni.portal.entity.Notification;
import com.alumni.portal.entity.User;
import com.alumni.portal.entity.enums.NotificationType;
import com.alumni.portal.entity.enums.Role;
import com.alumni.portal.repository.NotificationRepository;
import com.alumni.portal.repository.UserRepository;
import com.alumni.portal.security.JwtTokenProvider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class NotificationControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private UserRepository userRepository;
    @Autowired private NotificationRepository notificationRepository;
    @Autowired private JwtTokenProvider jwtTokenProvider;

    @AfterEach
    void tearDown() {
        notificationRepository.deleteAll();
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

    private String bearerTokenFor(User user) {
        return "Bearer " + jwtTokenProvider.generateTokenFromUsername(user.getEmail());
    }

    private Notification createNotification(User user, NotificationType type, String title, String message) {
        return notificationRepository.save(Notification.builder()
                .user(user)
                .type(type)
                .title(title)
                .message(message)
                .read(false)
                .build());
    }

    @Test
    @DisplayName("GET /api/notifications should return all notifications for the authenticated user")
    void getNotifications_returnsCurrentUsersNotifications() throws Exception {
        User user = createUser("Alice", "Brown", "alice@test.com");
        createNotification(user, NotificationType.NEW_CONNECTION_REQUEST, "New connection request", "Alice sent you a request.");

        mockMvc.perform(get("/api/notifications")
                        .header("Authorization", bearerTokenFor(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].userId").value(user.getId()));
    }

    @Test
    @DisplayName("GET /api/notifications/unread should only return unread notifications")
    void getUnreadNotifications_returnsUnreadOnly() throws Exception {
        User user = createUser("Alice", "Brown", "alice@test.com");
        Notification readNotification = createNotification(user, NotificationType.NEW_CONNECTION_REQUEST, "Read", "Old");
        readNotification.setRead(true);
        notificationRepository.save(readNotification);
        createNotification(user, NotificationType.CONNECTION_REQUEST_ACCEPTED, "Accepted", "Your request was accepted.");

        mockMvc.perform(get("/api/notifications/unread")
                        .header("Authorization", bearerTokenFor(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(1));
    }

    @Test
    @DisplayName("PUT /api/notifications/{id}/read should mark a notification as read")
    void markNotificationAsRead_returns200() throws Exception {
        User user = createUser("Alice", "Brown", "alice@test.com");
        Notification notification = createNotification(user, NotificationType.NEW_CONNECTION_REQUEST, "New connection request", "User sent you a request.");

        mockMvc.perform(put("/api/notifications/{id}/read", notification.getId())
                        .header("Authorization", bearerTokenFor(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.read").value(true));
    }

    @Test
    @DisplayName("DELETE /api/notifications/{id} should delete the user's own notification")
    void deleteNotification_successfullyDeletesOwnNotification() throws Exception {
        User user = createUser("Alice", "Brown", "alice@test.com");
        Notification notification = createNotification(user, NotificationType.NEW_CONNECTION_REQUEST, "New connection request", "User sent you a request.");

        mockMvc.perform(delete("/api/notifications/{id}", notification.getId())
                        .header("Authorization", bearerTokenFor(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("DELETE /api/notifications/{id} should reject another user's notification")
    void deleteAnotherUsersNotification_returns404() throws Exception {
        User userOne = createUser("Alice", "Brown", "alice@test.com");
        User userTwo = createUser("Bob", "White", "bob@test.com");
        Notification notification = createNotification(userTwo, NotificationType.NEW_CONNECTION_REQUEST, "New connection request", "User sent you a request.");

        mockMvc.perform(delete("/api/notifications/{id}", notification.getId())
                        .header("Authorization", bearerTokenFor(userOne)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Unauthenticated notification requests should be rejected")
    void unauthenticatedRequest_returns401() throws Exception {
        mockMvc.perform(get("/api/notifications"))
                .andExpect(status().isUnauthorized());
    }
}
