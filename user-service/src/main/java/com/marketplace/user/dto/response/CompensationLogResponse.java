package com.marketplace.user.dto.response;

import com.marketplace.user.entity.CompensationLog;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CompensationLogResponse {
    private Long id;
    private String bookingId;
    private Long userId;
    private String username;
    private Long transactionId;
    private CompensationLog.CompensationAction action;
    private BigDecimal amount;
    private CompensationLog.CompensationStatus status;
    private String reason;
    private LocalDateTime createdAt;
}
