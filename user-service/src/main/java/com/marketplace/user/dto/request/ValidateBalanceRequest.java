package com.marketplace.user.dto.request;

import lombok.Data;

import java.math.BigDecimal;

/**
 * Validate Balance Request DTO
 * Contains fields for validating wallet balance
 */
@Data
public class ValidateBalanceRequest {

    private Long userId;

    private BigDecimal amount;
}
