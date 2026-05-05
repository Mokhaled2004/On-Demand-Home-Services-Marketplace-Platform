package com.marketplace.notification.service;

import com.marketplace.notification.dto.BookingEventDto;
import com.marketplace.notification.dto.NotificationResponse;

import java.util.List;

public interface NotificationService {

    void handleBookingConfirmed(BookingEventDto event);

    void handleBookingFailed(BookingEventDto event);

    void handleBookingCancelled(BookingEventDto event);

    List<NotificationResponse> getNotificationsForUser(Long userId);

    List<NotificationResponse> getUnreadNotificationsForUser(Long userId);

    List<NotificationResponse> getNotificationsForBooking(Long bookingId);

    NotificationResponse markAsRead(Long notificationId);
}
