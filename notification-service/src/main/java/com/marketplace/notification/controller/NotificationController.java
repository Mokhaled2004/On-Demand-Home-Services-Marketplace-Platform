package com.marketplace.notification.controller;

import com.marketplace.notification.dto.NotificationResponse;
import com.marketplace.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST endpoints for notifications.
 * No JWT auth — notifications are read by userId passed in the path.
 * Base URL: /notifications
 */
@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
@Slf4j
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * GET /notifications/user/{userId}
     * Get all notifications for a user (read + unread), newest first.
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<Map<String, Object>> getNotificationsForUser(@PathVariable Long userId) {
        log.info("Fetching all notifications for userId={}", userId);
        List<NotificationResponse> notifications = notificationService.getNotificationsForUser(userId);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Notifications retrieved successfully",
                "data", notifications
        ));
    }

    /**
     * GET /notifications/user/{userId}/unread
     * Get only unread notifications for a user.
     */
    @GetMapping("/user/{userId}/unread")
    public ResponseEntity<Map<String, Object>> getUnreadNotifications(@PathVariable Long userId) {
        log.info("Fetching unread notifications for userId={}", userId);
        List<NotificationResponse> notifications = notificationService.getUnreadNotificationsForUser(userId);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Unread notifications retrieved successfully",
                "data", notifications,
                "count", notifications.size()
        ));
    }

    /**
     * GET /notifications/booking/{bookingId}
     * Get all notifications related to a specific booking.
     */
    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<Map<String, Object>> getNotificationsForBooking(@PathVariable Long bookingId) {
        log.info("Fetching notifications for bookingId={}", bookingId);
        List<NotificationResponse> notifications = notificationService.getNotificationsForBooking(bookingId);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Booking notifications retrieved successfully",
                "data", notifications
        ));
    }

    /**
     * PUT /notifications/{id}/read
     * Mark a notification as read.
     */
    @PutMapping("/{id}/read")
    public ResponseEntity<Map<String, Object>> markAsRead(@PathVariable Long id) {
        log.info("Marking notification {} as read", id);
        NotificationResponse notification = notificationService.markAsRead(id);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Notification marked as read",
                "data", notification
        ));
    }
}
