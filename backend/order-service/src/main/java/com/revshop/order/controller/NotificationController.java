package com.revshop.order.controller;

import com.revshop.order.dto.NotificationResponse;
import com.revshop.order.service.NotificationService;
import com.revshop.order.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public ResponseEntity<List<NotificationResponse>> getUserNotifications(HttpServletRequest request) {
        Long userId = JwtUtil.getUserIdFromRequest(request);
        List<NotificationResponse> notifications = notificationService.getUserNotifications(userId);
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/unread")
    public ResponseEntity<List<NotificationResponse>> getUnreadNotifications(HttpServletRequest request) {
        Long userId = JwtUtil.getUserIdFromRequest(request);
        List<NotificationResponse> notifications = notificationService.getUnreadNotifications(userId);
        return ResponseEntity.ok(notifications);
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<String> markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return ResponseEntity.ok("Notification marked as read");
    }

    @PutMapping("/read-all")
    public ResponseEntity<String> markAllAsRead(HttpServletRequest request) {
        Long userId = JwtUtil.getUserIdFromRequest(request);
        notificationService.markAllAsRead(userId);
        return ResponseEntity.ok("All notifications marked as read");
    }
}
