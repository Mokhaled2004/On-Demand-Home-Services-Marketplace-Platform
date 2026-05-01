package com.marketplace.user.dto.response;

import lombok.Data;

@Data
public class WalletResponse {
    private long userId;
    private long balance;
    private String currency;
}
