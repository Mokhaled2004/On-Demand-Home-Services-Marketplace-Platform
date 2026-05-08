package com.marketplace.notification.service;

import com.marketplace.notification.dto.BookingEventDto;
import com.marketplace.notification.dto.NotificationResponse;
import com.marketplace.notification.entity.Notification;
import com.marketplace.notification.entity.NotificationLog;
import com.marketplace.notification.repository.NotificationLogRepository;
import com.marketplace.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationLogRepository notificationLogRepository;

    // ===== RabbitMQ Event Handlers =====

    @Override
    @Transactional
    public void handleBookingConfirmed(BookingEventDto event) {
        log.info("Handling BOOKING_CONFIRMED for bookingId={}", event.getBookingId());

        // Notify customer
        createAndLog(
            event.getCustomerId(),
            event.getBookingId(),
            Notification.NotificationType.BOOKING_CONFIRMED,
            "Booking Confirmed!",
            "Your booking #" + event.getBookingId() + " has been confirmed. "
                + "Amount charged: $" + event.getAmount() + ". Thank you!"
        );

        // Notify provider with customer name
        if (event.getProviderId() != null) {
            String customerName = event.getCustomerName() != null ? event.getCustomerName() : "Customer";
            createAndLog(
                event.getProviderId(),
                event.getBookingId(),
                Notification.NotificationType.BOOKING_CONFIRMED,
                "New Booking Received!",
                "You have a new confirmed booking #" + event.getBookingId() + " from " + customerName + ". "
                    + "Service amount: $" + event.getAmount() + "."
            );
        }
    }

    @Override
    @Transactional
    public void handleBookingFailed(BookingEventDto event) {
        log.info("Handling BOOKING_FAILED for bookingId={}", event.getBookingId());

        // Notify customer only
        String reason = event.getReason() != null ? event.getReason() : "Insufficient wallet balance";
        createAndLog(
            event.getCustomerId(),
            event.getBookingId(),
            Notification.NotificationType.BOOKING_FAILED,
            "Booking Failed",
            "Your booking #" + event.getBookingId() + " could not be completed. "
                + "Reason: " + reason + ". No amount was charged."
        );
    }

    @Override
    @Transactional
    public void handleBookingCancelled(BookingEventDto event) {
        log.info("Handling BOOKING_CANCELLED for bookingId={}", event.getBookingId());

        // Notify customer
        createAndLog(
            event.getCustomerId(),
            event.getBookingId(),
            Notification.NotificationType.BOOKING_CANCELLED,
            "Booking Cancelled",
            "Your booking #" + event.getBookingId() + " has been cancelled. "
                + "Amount of $" + event.getAmount() + " has been refunded to your wallet."
        );

        // Notify provider with customer name
        if (event.getProviderId() != null) {
            String customerName = event.getCustomerName() != null ? event.getCustomerName() : "Customer";
            createAndLog(
                event.getProviderId(),
                event.getBookingId(),
                Notification.NotificationType.BOOKING_CANCELLED,
                "Booking Cancelled by Customer",
                "Booking #" + event.getBookingId() + " from " + customerName + " has been cancelled."
            );
        }
    }

    // ===== REST Query Methods =====

    @Override
    public List<NotificationResponse> getNotificationsForUser(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream().map(NotificationResponse::from).collect(Collectors.toList());
    }

    @Override
    public List<NotificationResponse> getUnreadNotificationsForUser(Long userId) {
        return notificationRepository.findByUserIdAndReadStatusFalseOrderByCreatedAtDesc(userId)
                .stream().map(NotificationResponse::from).collect(Collectors.toList());
    }

    @Override
    public List<NotificationResponse> getNotificationsForBooking(Long bookingId) {
        return notificationRepository.findByBookingIdOrderByCreatedAtDesc(bookingId)
                .stream().map(NotificationResponse::from).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public NotificationResponse markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found: " + notificationId));

        notification.setReadStatus(true);
        notification.setReadAt(LocalDateTime.now());
        notificationRepository.save(notification);

        log.info("Notification {} marked as read", notificationId);
        return NotificationResponse.from(notification);
    }

    // ===== Private Helper =====

    private void createAndLog(Long userId, Long bookingId,
                               Notification.NotificationType type,
                               String title, String message) {
        // Save notification
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setBookingId(bookingId);
        notification.setType(type);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setReadStatus(false);
        notification = notificationRepository.save(notification);

        // Log delivery
        NotificationLog logEntry = new NotificationLog();
        logEntry.setNotificationId(notification.getId());
        logEntry.setStatus(NotificationLog.DeliveryStatus.SENT);
        logEntry.setSentAt(LocalDateTime.now());
        logEntry.setRetryCount(0);
        notificationLogRepository.save(logEntry);

        log.info("Notification created and logged: userId={}, type={}, bookingId={}",
                userId, type, bookingId);
    }
}
