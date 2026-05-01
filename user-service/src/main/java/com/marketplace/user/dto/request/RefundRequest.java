package com.marketplace.user.dto.request;

import lombok.Data;

import java.math.BigDecimal;

/**
 * Refund Request DTO
 * Contains fields for refunding wallet balance
 */
@Data
public class RefundRequest {

    private Long userId;

    private BigDecimal amount;

    private String bookingId;

    private String idempotencyKey;
}
