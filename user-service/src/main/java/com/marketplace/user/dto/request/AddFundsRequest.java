package com.marketplace.user.dto.request;

import java.math.BigDecimal;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;

import org.springframework.validation.annotation.Validated;

import lombok.Data;

@Data @Validated
public class AddFundsRequest {

    @NotNull private long userId;
    @NotNull @DecimalMin("0.01") private BigDecimal amount;
}
