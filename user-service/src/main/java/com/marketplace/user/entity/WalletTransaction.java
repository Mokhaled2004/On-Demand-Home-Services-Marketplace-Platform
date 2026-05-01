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
@Table(name = "wallet_transactions", indexes = {
    @Index(name = "idx_wallet_id", columnList = "wallet_id"),
    @Index(name = "idx_status", columnList = "status"),
    @Index(name = "idx_idempotency_key", columnList = "idempotency_key", unique = true)
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class WalletTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long walletId;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private TransactionType transactionType; // DEBIT, CREDIT

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private TransactionStatus status; // PENDING, SUCCESS, FAILED

    @Column(length = 100)
    private String referenceId; // booking_id or order_id

    @Column(length = 255)
    private String description;

    @Column(unique = true, length = 100)
    private String idempotencyKey; // Prevent duplicate transactions

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public enum TransactionType {
        DEBIT,
        CREDIT
    }

    public enum TransactionStatus {
        PENDING,
        SUCCESS,
        FAILED
    }
}
