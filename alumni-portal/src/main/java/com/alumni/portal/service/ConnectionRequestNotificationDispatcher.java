package com.alumni.portal.service;

import com.alumni.portal.dto.response.NotificationResponse;
import com.alumni.portal.entity.enums.NotificationType;

public interface ConnectionRequestNotificationDispatcher {

    NotificationResponse createNotification(Long userId, NotificationType type, String title, String message);
}
