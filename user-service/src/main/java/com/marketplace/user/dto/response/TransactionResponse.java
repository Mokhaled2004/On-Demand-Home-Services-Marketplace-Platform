package com.marketplace.user.dto.response;

import com.marketplace.user.entity.WalletTransaction;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class TransactionResponse {
    private Long id;
    private Long walletId;
    private Long userId;
    private String username;
    private WalletTransaction.TransactionType transactionType;
    private BigDecimal amount;
    private String referenceId;
    private String description;
    private WalletTransaction.TransactionStatus status;
    private LocalDateTime createdAt;
}
