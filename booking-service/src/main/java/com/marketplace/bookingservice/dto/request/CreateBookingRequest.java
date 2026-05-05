package com.marketplace.bookingservice.dto.request;

import java.time.LocalDateTime;

/**
 * Request body for POST /api/bookings
 * customerId is NOT included — extracted from JWT token.
 * serviceStart and serviceEnd are OPTIONAL — if not provided,
 * they are taken from the offer's availableFrom / availableTo.
 */
public class CreateBookingRequest {

    private Long serviceOfferId;
    private LocalDateTime serviceStart;   // optional
    private LocalDateTime serviceEnd;     // optional
    private String idempotencyKey;        // optional

    public CreateBookingRequest() {}

    public Long getServiceOfferId() { return serviceOfferId; }
    public void setServiceOfferId(Long serviceOfferId) { this.serviceOfferId = serviceOfferId; }

    public LocalDateTime getServiceStart() { return serviceStart; }
    public void setServiceStart(LocalDateTime serviceStart) { this.serviceStart = serviceStart; }

    public LocalDateTime getServiceEnd() { return serviceEnd; }
    public void setServiceEnd(LocalDateTime serviceEnd) { this.serviceEnd = serviceEnd; }

    public String getIdempotencyKey() { return idempotencyKey; }
    public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }
}
