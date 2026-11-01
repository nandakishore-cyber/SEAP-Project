package com.alumni.portal.service;

import com.alumni.portal.dto.response.NotificationResponse;
import com.alumni.portal.entity.Notification;
import com.alumni.portal.entity.User;
import com.alumni.portal.entity.enums.Role;
import com.alumni.portal.exception.ResourceNotFoundException;
import com.alumni.portal.repository.NotificationRepository;
import com.alumni.portal.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public NotificationService(NotificationRepository notificationRepository,
                               UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public NotificationResponse notify(Long recipientId, String type, String title,
                                       String message, Long relatedId) {
        User recipient = userRepository.findById(recipientId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", recipientId));
        return map(notificationRepository.save(Notification.builder()
                .recipient(recipient)
                .type(type)
                .title(title)
                .message(message)
                .relatedId(relatedId)
                .build()));
    }

    @Transactional
    public NotificationResponse sendToStudent(Long senderId, Long studentId, String title, String message) {
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", senderId));
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", studentId));
        if (sender.getRole() != Role.ROLE_ALUMNI && sender.getRole() != Role.ROLE_ADMIN) {
            throw new IllegalArgumentException("Only alumni can send direct student notifications");
        }
        if (student.getRole() != Role.ROLE_STUDENT) {
            throw new IllegalArgumentException("Notifications can only be sent to students");
        }
        return notify(studentId, "DIRECT_MESSAGE", title, message, null);
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> getNotifications(Long recipientId) {
        return notificationRepository.findByRecipientIdOrderByCreatedAtDesc(recipientId)
                .stream().map(this::map).toList();
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(Long recipientId) {
        return notificationRepository.countByRecipientIdAndReadAtIsNull(recipientId);
    }

    @Transactional
    public NotificationResponse markRead(Long recipientId, Long notificationId) {
        Notification notification = getOwnedNotification(recipientId, notificationId);
        notification.setReadAt(notification.getReadAt() == null ? LocalDateTime.now() : notification.getReadAt());
        return map(notificationRepository.save(notification));
    }

    @Transactional
    public void markAllRead(Long recipientId) {
        notificationRepository.findByRecipientIdOrderByCreatedAtDesc(recipientId)
                .stream()
                .filter(notification -> notification.getReadAt() == null)
                .forEach(notification -> notification.setReadAt(LocalDateTime.now()));
    }

    private Notification getOwnedNotification(Long recipientId, Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification", "id", notificationId));
        if (!notification.getRecipient().getId().equals(recipientId)) {
            throw new ResourceNotFoundException("Notification", "id", notificationId);
        }
        return notification;
    }

    private NotificationResponse map(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .type(notification.getType())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .relatedId(notification.getRelatedId())
                .read(notification.getReadAt() != null)
                .createdAt(notification.getCreatedAt())
                .build();
    }
}
