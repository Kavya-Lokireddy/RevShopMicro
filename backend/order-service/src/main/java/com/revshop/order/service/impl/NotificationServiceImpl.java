package com.revshop.order.service.impl;

import com.revshop.order.dto.NotificationResponse;
import com.revshop.order.entity.Notification;
import com.revshop.order.entity.NotificationType;
import com.revshop.order.exception.OrderNotFoundException;
import com.revshop.order.repository.NotificationRepository;
import com.revshop.order.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificationServiceImpl implements NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationServiceImpl.class);

    private final NotificationRepository notificationRepository;

    public NotificationServiceImpl(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Override
    @Transactional
    public void createNotification(Long userId, String message, NotificationType type, Long referenceId) {
        log.info("Creating notification for userId: {}, type: {}, referenceId: {}", userId, type, referenceId);
        Notification notification = new Notification(userId, message, type, referenceId);
        notificationRepository.save(notification);
        log.debug("Notification created successfully for userId: {}", userId);
    }

    @Override
    public List<NotificationResponse> getUserNotifications(Long userId) {
        log.info("Fetching all notifications for userId: {}", userId);
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::mapToNotificationResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<NotificationResponse> getUnreadNotifications(Long userId) {
        log.info("Fetching unread notifications for userId: {}", userId);
        return notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::mapToNotificationResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void markAsRead(Long notificationId) {
        log.info("Marking notification as read, notificationId: {}", notificationId);
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> {
                    log.warn("Notification not found with id: {}", notificationId);
                    return new OrderNotFoundException("Notification not found with id: " + notificationId);
                });
        notification.setIsRead(true);
        notificationRepository.save(notification);
        log.debug("Notification marked as read, notificationId: {}", notificationId);
    }

    @Override
    @Transactional
    public void markAllAsRead(Long userId) {
        log.info("Marking all notifications as read for userId: {}", userId);
        List<Notification> unreadNotifications = notificationRepository
                .findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId);

        log.debug("Found {} unread notifications for userId: {}", unreadNotifications.size(), userId);
        unreadNotifications.forEach(notification -> notification.setIsRead(true));
        notificationRepository.saveAll(unreadNotifications);
        log.info("All notifications marked as read for userId: {}", userId);
    }

    private NotificationResponse mapToNotificationResponse(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getUserId(),
                notification.getMessage(),
                notification.getType(),
                notification.getReferenceId(),
                notification.getIsRead(),
                notification.getCreatedAt()
        );
    }
}
