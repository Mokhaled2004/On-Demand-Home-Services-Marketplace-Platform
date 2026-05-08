package com.marketplace.notification.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Maps the JSON payload published by Booking Service to RabbitMQ.
 *
 * booking.confirmed: { eventType, bookingId, customerId, customerName, providerId, amount }
 * booking.failed:    { eventType, bookingId, customerId, reason }
 * booking.cancelled: { eventType, bookingId, customerId, customerName, providerId, amount }
 */
@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class BookingEventDto {

    private String eventType;
    private Long bookingId;
    private Long customerId;
    private String customerName;
    private Long providerId;
    private BigDecimal amount;
    private String reason;
}
