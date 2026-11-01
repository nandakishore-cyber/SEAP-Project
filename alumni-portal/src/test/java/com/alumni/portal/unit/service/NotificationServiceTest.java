package com.alumni.portal.unit.service;

import com.alumni.portal.entity.Notification;
import com.alumni.portal.entity.User;
import com.alumni.portal.entity.enums.Role;
import com.alumni.portal.repository.NotificationRepository;
import com.alumni.portal.repository.UserRepository;
import com.alumni.portal.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
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

    @Test
    void notify_savesNotificationForRecipient() {
        User user = User.builder().id(1L).role(Role.ROLE_STUDENT).build();
        Notification notification = Notification.builder().id(2L).recipient(user)
                .type("MENTORSHIP_REQUEST").title("Request").message("Message").build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);

        assertThat(notificationService.notify(1L, "MENTORSHIP_REQUEST", "Request", "Message", 4L).getId())
                .isEqualTo(2L);
        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    void markAllRead_updatesOnlyUnreadNotifications() {
        User user = User.builder().id(1L).role(Role.ROLE_STUDENT).build();
        Notification unread = Notification.builder().id(1L).recipient(user).build();
        Notification read = Notification.builder().id(2L).recipient(user).build();
        read.setReadAt(java.time.LocalDateTime.now());
        when(notificationRepository.findByRecipientIdOrderByCreatedAtDesc(1L))
                .thenReturn(List.of(unread, read));

        notificationService.markAllRead(1L);

        assertThat(unread.getReadAt()).isNotNull();
        assertThat(read.getReadAt()).isNotNull();
    }
}
