package com.marketplace.user.dto.request;

import java.math.BigDecimal;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

import lombok.Data;

@Data
public class DeductBalanceRequest {

    // userId is NOT accepted from the body - it is taken from the JWT token in the controller
    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be greater than 0")
    private BigDecimal amount;

    @NotBlank(message = "Booking ID is required")
    private String bookingId;
}
