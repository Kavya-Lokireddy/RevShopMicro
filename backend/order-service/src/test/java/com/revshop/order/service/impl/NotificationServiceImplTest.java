package com.revshop.order.service.impl;

import com.revshop.order.dto.NotificationResponse;
import com.revshop.order.entity.Notification;
import com.revshop.order.entity.NotificationType;
import com.revshop.order.exception.OrderNotFoundException;
import com.revshop.order.repository.NotificationRepository;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    private Notification testNotification;

    @BeforeEach
    void setUp() {
        testNotification = new Notification();
        testNotification.setId(1L);
        testNotification.setUserId(1L);
        testNotification.setMessage("Your order #100 has been placed successfully!");
        testNotification.setType(NotificationType.ORDER_PLACED);
        testNotification.setReferenceId(100L);
        testNotification.setIsRead(false);
        testNotification.setCreatedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("Test createNotification - should save notification successfully")
    void testCreateNotification_Success() {
        when(notificationRepository.save(any(Notification.class))).thenReturn(testNotification);

        notificationService.createNotification(1L, "Test message", NotificationType.ORDER_PLACED, 100L);

        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    @DisplayName("Test getUserNotifications - should return all user notifications")
    void testGetUserNotifications_Success() {
        when(notificationRepository.findByUserIdOrderByCreatedAtDesc(1L))
                .thenReturn(List.of(testNotification));

        List<NotificationResponse> responses = notificationService.getUserNotifications(1L);

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals("Your order #100 has been placed successfully!", responses.get(0).getMessage());
        assertEquals(NotificationType.ORDER_PLACED, responses.get(0).getType());
    }

    @Test
    @DisplayName("Test getUnreadNotifications - should return only unread notifications")
    void testGetUnreadNotifications_Success() {
        when(notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(1L))
                .thenReturn(List.of(testNotification));

        List<NotificationResponse> responses = notificationService.getUnreadNotifications(1L);

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertFalse(responses.get(0).getIsRead());
    }

    @Test
    @DisplayName("Test markAsRead - should mark notification as read")
    void testMarkAsRead_Success() {
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(testNotification));
        when(notificationRepository.save(any(Notification.class))).thenReturn(testNotification);

        notificationService.markAsRead(1L);

        assertTrue(testNotification.getIsRead());
        verify(notificationRepository).save(testNotification);
    }

    @Test
    @DisplayName("Test markAsRead - should throw exception when notification not found")
    void testMarkAsRead_NotFound() {
        when(notificationRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(OrderNotFoundException.class, () -> notificationService.markAsRead(999L));
    }

    @Test
    @DisplayName("Test markAllAsRead - should mark all notifications as read for user")
    void testMarkAllAsRead_Success() {
        Notification notification2 = new Notification();
        notification2.setId(2L);
        notification2.setUserId(1L);
        notification2.setMessage("Order shipped");
        notification2.setType(NotificationType.ORDER_SHIPPED);
        notification2.setReferenceId(101L);
        notification2.setIsRead(false);
        notification2.setCreatedAt(LocalDateTime.now());

        List<Notification> unreadNotifications = List.of(testNotification, notification2);
        when(notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(1L))
                .thenReturn(unreadNotifications);

        notificationService.markAllAsRead(1L);

        assertTrue(testNotification.getIsRead());
        assertTrue(notification2.getIsRead());
        verify(notificationRepository).saveAll(unreadNotifications);
    }
}
