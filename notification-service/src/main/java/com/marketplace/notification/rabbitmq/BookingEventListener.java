package com.marketplace.notification.rabbitmq;

import com.marketplace.notification.dto.BookingEventDto;
import com.marketplace.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * RabbitMQ consumer for booking events.
 * Listens on 3 queues and delegates to NotificationService.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class BookingEventListener {

    private final NotificationService notificationService;

    /**
     * Listens on booking.confirmed.queue
     * Triggered when a booking is successfully confirmed and payment deducted.
     * Creates notifications for both customer and provider.
     */
    @RabbitListener(queues = RabbitMQConfig.QUEUE_CONFIRMED)
    public void handleBookingConfirmed(BookingEventDto event) {
        log.info("Received BOOKING_CONFIRMED event: bookingId={}, customerId={}, providerId={}",
                event.getBookingId(), event.getCustomerId(), event.getProviderId());
        try {
            notificationService.handleBookingConfirmed(event);
        } catch (Exception e) {
            log.error("Failed to process BOOKING_CONFIRMED event for bookingId={}: {}",
                    event.getBookingId(), e.getMessage());
        }
    }

    /**
     * Listens on booking.failed.queue
     * Triggered when a booking fails (insufficient balance).
     * Creates notification for customer only.
     */
    @RabbitListener(queues = RabbitMQConfig.QUEUE_FAILED)
    public void handleBookingFailed(BookingEventDto event) {
        log.info("Received BOOKING_FAILED event: bookingId={}, customerId={}, reason={}",
                event.getBookingId(), event.getCustomerId(), event.getReason());
        try {
            notificationService.handleBookingFailed(event);
        } catch (Exception e) {
            log.error("Failed to process BOOKING_FAILED event for bookingId={}: {}",
                    event.getBookingId(), e.getMessage());
        }
    }

    /**
     * Listens on booking.cancelled.queue
     * Triggered when a customer cancels a confirmed booking.
     * Creates notifications for both customer and provider.
     */
    @RabbitListener(queues = RabbitMQConfig.QUEUE_CANCELLED)
    public void handleBookingCancelled(BookingEventDto event) {
        log.info("Received BOOKING_CANCELLED event: bookingId={}, customerId={}, providerId={}",
                event.getBookingId(), event.getCustomerId(), event.getProviderId());
        try {
            notificationService.handleBookingCancelled(event);
        } catch (Exception e) {
            log.error("Failed to process BOOKING_CANCELLED event for bookingId={}: {}",
                    event.getBookingId(), e.getMessage());
        }
    }
}
