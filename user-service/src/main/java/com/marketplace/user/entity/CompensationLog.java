package com.marketplace.user.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "compensation_log", indexes = {
    @Index(name = "idx_booking_id", columnList = "booking_id"),
    @Index(name = "idx_user_id", columnList = "user_id"),
    @Index(name = "idx_status", columnList = "status")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class CompensationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String bookingId;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = true)
    private Long transactionId;

    @Column(nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private CompensationAction action; // DEDUCT, REFUND

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private CompensationStatus status; // PENDING, COMPLETED, FAILED

    @Column(length = 255)
    private String reason; // Optional reason for refund or failure

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public enum CompensationAction {
        DEDUCT,
        REFUND
    }

    public enum CompensationStatus {
        PENDING,
        COMPLETED,
        FAILED
    }
}
