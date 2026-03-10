package com.revshop.order.service;

import com.revshop.order.dto.NotificationResponse;
import com.revshop.order.entity.NotificationType;

import java.util.List;

public interface NotificationService {

    void createNotification(Long userId, String message, NotificationType type, Long referenceId);

    List<NotificationResponse> getUserNotifications(Long userId);

    List<NotificationResponse> getUnreadNotifications(Long userId);

    void markAsRead(Long notificationId);

    void markAllAsRead(Long userId);
}
