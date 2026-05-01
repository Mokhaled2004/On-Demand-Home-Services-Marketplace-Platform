package com.marketplace.user.mapper;

import com.marketplace.user.dto.response.WalletResponse;
import com.marketplace.user.entity.Wallet;
import org.springframework.stereotype.Component;

/**
 * Wallet Mapper
 * Maps Wallet entity to WalletResponse DTO
 */
@Component
public class WalletMapper {

    /**
     * Convert Wallet entity to WalletResponse DTO
     * @param wallet the Wallet entity
     * @return WalletResponse DTO
     */
    public WalletResponse toWalletResponse(Wallet wallet) {
        if (wallet == null) {
            return null;
        }

        WalletResponse response = new WalletResponse();
        response.setUserId(wallet.getUserId());
        response.setBalance(wallet.getBalance().longValue());
        response.setCurrency(wallet.getCurrency());

        return response;
    }
}
