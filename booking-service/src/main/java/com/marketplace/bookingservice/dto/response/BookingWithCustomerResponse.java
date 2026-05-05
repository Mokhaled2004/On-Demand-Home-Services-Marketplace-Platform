package com.marketplace.bookingservice.dto.response;

import com.marketplace.bookingservice.entity.Booking;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Extended booking response that includes customer name.
 * Used for provider's "view completed services" endpoint (Requirement 9).
 */
public class BookingWithCustomerResponse {

    private Long id;
    private Long customerId;
    private String customerName;   // fetched from User Service
    private String customerEmail;  // fetched from User Service
    private Long serviceOfferId;
    private Long providerId;
    private LocalDateTime bookingDate;
    private LocalDateTime serviceStart;
    private LocalDateTime serviceEnd;
    private BigDecimal amount;
    private String status;
    private LocalDateTime createdAt;

    public BookingWithCustomerResponse() {}

    public static BookingWithCustomerResponse from(Booking booking,
                                                    String customerName,
                                                    String customerEmail) {
        BookingWithCustomerResponse r = new BookingWithCustomerResponse();
        r.id             = booking.getId();
        r.customerId     = booking.getCustomerId();
        r.customerName   = customerName;
        r.customerEmail  = customerEmail;
        r.serviceOfferId = booking.getServiceOfferId();
        r.providerId     = booking.getProviderId();
        r.bookingDate    = booking.getBookingDate();
        r.serviceStart   = booking.getServiceStart();
        r.serviceEnd     = booking.getServiceEnd();
        r.amount         = booking.getAmount();
        r.status         = booking.getStatus().name();
        r.createdAt      = booking.getCreatedAt();
        return r;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getCustomerEmail() { return customerEmail; }
    public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }

    public Long getServiceOfferId() { return serviceOfferId; }
    public void setServiceOfferId(Long serviceOfferId) { this.serviceOfferId = serviceOfferId; }

    public Long getProviderId() { return providerId; }
    public void setProviderId(Long providerId) { this.providerId = providerId; }

    public LocalDateTime getBookingDate() { return bookingDate; }
    public void setBookingDate(LocalDateTime bookingDate) { this.bookingDate = bookingDate; }

    public LocalDateTime getServiceStart() { return serviceStart; }
    public void setServiceStart(LocalDateTime serviceStart) { this.serviceStart = serviceStart; }

    public LocalDateTime getServiceEnd() { return serviceEnd; }
    public void setServiceEnd(LocalDateTime serviceEnd) { this.serviceEnd = serviceEnd; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
