package com.marketplace.bookingservice.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * JPA Entity mapping to the 'bookings' table in Neon PostgreSQL.
 * No cross-DB foreign keys — customer_id, service_offer_id, provider_id
 * are plain BIGINT columns validated via REST calls to other services.
 */
@Entity
@Table(name = "bookings")
public class Booking {

    // ===== Status Enum =====
    public enum BookingStatus {
        PENDING, CONFIRMED, COMPLETED, FAILED, CANCELLED
    }

    // ===== Fields =====

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(name = "service_offer_id", nullable = false)
    private Long serviceOfferId;

    @Column(name = "provider_id", nullable = false)
    private Long providerId;

    @Column(name = "booking_date", nullable = false)
    private LocalDateTime bookingDate;

    @Column(name = "service_start", nullable = false)
    private LocalDateTime serviceStart;

    @Column(name = "service_end", nullable = false)
    private LocalDateTime serviceEnd;

    @Column(name = "amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private BookingStatus status = BookingStatus.PENDING;

    @Column(name = "idempotency_key", unique = true, length = 100)
    private String idempotencyKey;

    @Column(name = "event_published")
    private Boolean eventPublished = false;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ===== Lifecycle Callbacks =====

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (bookingDate == null) {
            bookingDate = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // ===== Constructors =====

    public Booking() {}

    // ===== Getters & Setters =====

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }

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

    public BookingStatus getStatus() { return status; }
    public void setStatus(BookingStatus status) { this.status = status; }

    public String getIdempotencyKey() { return idempotencyKey; }
    public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }

    public Boolean getEventPublished() { return eventPublished; }
    public void setEventPublished(Boolean eventPublished) { this.eventPublished = eventPublished; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
