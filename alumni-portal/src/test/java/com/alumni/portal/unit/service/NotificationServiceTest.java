package com.alumni.portal.unit.service;

import com.alumni.portal.dto.response.NotificationResponse;
import com.alumni.portal.entity.Notification;
import com.alumni.portal.entity.User;
import com.alumni.portal.entity.enums.NotificationType;
import com.alumni.portal.entity.enums.Role;
import com.alumni.portal.exception.ResourceNotFoundException;
import com.alumni.portal.repository.NotificationRepository;
import com.alumni.portal.repository.UserRepository;
import com.alumni.portal.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private NotificationService notificationService;

    private User currentUser;

    @BeforeEach
    void setUp() {
        currentUser = User.builder()
                .id(1L)
                .firstName("Alice")
                .lastName("Brown")
                .email("alice@test.com")
                .password("encoded")
                .role(Role.ROLE_ALUMNI)
                .enabled(true)
                .build();
    }

    @Test
    @DisplayName("createNotification should save and return a notification")
    void createNotification_savesNotification() {
        Notification saved = Notification.builder()
                .id(10L)
                .user(currentUser)
                .type(NotificationType.NEW_CONNECTION_REQUEST)
                .title("New connection request")
                .message("Alice sent you a connection request.")
                .read(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(currentUser));
        when(notificationRepository.save(any(Notification.class))).thenReturn(saved);

        NotificationResponse response = notificationService.createNotification(
                1L,
                NotificationType.NEW_CONNECTION_REQUEST,
                "New connection request",
                "Alice sent you a connection request.");

        assertThat(response.getUserId()).isEqualTo(1L);
        assertThat(response.getType()).isEqualTo(NotificationType.NEW_CONNECTION_REQUEST);
        assertThat(response.getTitle()).isEqualTo("New connection request");
        assertThat(response.isRead()).isFalse();
    }

    @Test
    @DisplayName("getNotifications should return all notifications for a user")
    void getNotifications_returnsAllUserNotifications() {
        when(userRepository.existsById(1L)).thenReturn(true);
        Notification notification = Notification.builder()
                .id(11L)
                .user(currentUser)
                .type(NotificationType.NEW_CONNECTION_REQUEST)
                .title("New connection request")
                .message("Alice sent you a connection request.")
                .read(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        when(notificationRepository.findByUserIdOrderByCreatedAtDesc(1L)).thenReturn(List.of(notification));

        List<NotificationResponse> responses = notificationService.getNotifications(1L);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getUserId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("getUnreadNotifications should return unread notifications")
    void getUnreadNotifications_returnsUnreadOnly() {
        when(userRepository.existsById(1L)).thenReturn(true);
        Notification notification = Notification.builder()
                .id(12L)
                .user(currentUser)
                .type(NotificationType.NEW_CONNECTION_REQUEST)
                .title("New connection request")
                .message("Alice sent you a connection request.")
                .read(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        when(notificationRepository.findByUserIdAndReadFalseOrderByCreatedAtDesc(1L)).thenReturn(List.of(notification));

        List<NotificationResponse> responses = notificationService.getUnreadNotifications(1L);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).isRead()).isFalse();
    }

    @Test
    @DisplayName("markAsRead should mark a notification as read")
    void markAsRead_marksNotificationRead() {
        Notification notification = Notification.builder()
                .id(13L)
                .user(currentUser)
                .type(NotificationType.NEW_CONNECTION_REQUEST)
                .title("New connection request")
                .message("Alice sent you a connection request.")
                .read(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(notificationRepository.findByIdAndUserId(13L, 1L)).thenReturn(Optional.of(notification));
        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> invocation.getArgument(0));

        NotificationResponse response = notificationService.markAsRead(1L, 13L);

        assertThat(response.isRead()).isTrue();
    }

    @Test
    @DisplayName("deleteNotification should delete the user's own notification")
    void deleteNotification_deletesUsersOwnNotification() {
        Notification notification = Notification.builder()
                .id(14L)
                .user(currentUser)
                .type(NotificationType.NEW_CONNECTION_REQUEST)
                .title("New connection request")
                .message("Alice sent you a connection request.")
                .read(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(notificationRepository.findByIdAndUserId(14L, 1L)).thenReturn(Optional.of(notification));

        notificationService.deleteNotification(1L, 14L);

        verify(notificationRepository).delete(notification);
    }

    @Test
    @DisplayName("markAsRead should prevent access to another user's notification")
    void markAsRead_rejectsAnotherUsersNotification() {
        when(notificationRepository.findByIdAndUserId(15L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> notificationService.markAsRead(1L, 15L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Notification");
    }

    @Test
    @DisplayName("deleteNotification should prevent deletion of another user's notification")
    void deleteNotification_rejectsAnotherUsersNotification() {
        when(notificationRepository.findByIdAndUserId(16L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> notificationService.deleteNotification(1L, 16L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Notification");
    }
}
