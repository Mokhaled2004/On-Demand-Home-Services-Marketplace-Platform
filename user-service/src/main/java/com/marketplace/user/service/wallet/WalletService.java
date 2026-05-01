package com.marketplace.user.service.wallet;

import com.marketplace.user.entity.Wallet;

import java.math.BigDecimal;

/**
 * Wallet Service Interface
 * Defines business logic for wallet operations
 */
public interface WalletService {

    /**
     * Get wallet balance for user
     * @param userId user ID
     * @return Wallet entity with current balance
     * @throws com.marketplace.user.exception.UserNotFoundException if user not found
     */
    Wallet getWalletBalance(Long userId);

    /**
     * Deduct amount from wallet (for booking payment)
     * @param userId user ID
     * @param amount amount to deduct
     * @param bookingId booking ID (for tracking)
     * @param idempotencyKey unique key to prevent duplicate deductions
     * @return updated Wallet entity
     * @throws com.marketplace.user.exception.UserNotFoundException if user not found
     * @throws com.marketplace.user.exception.InsufficientBalanceException if balance < amount
     */
    Wallet deductBalance(Long userId, BigDecimal amount, Long bookingId, String idempotencyKey);

    /**
     * Refund amount to wallet (for booking cancellation)
     * @param userId user ID
     * @param amount amount to refund
     * @param bookingId booking ID (for tracking)
     * @param idempotencyKey unique key to prevent duplicate refunds
     * @return updated Wallet entity
     * @throws com.marketplace.user.exception.UserNotFoundException if user not found
     */
    Wallet refundBalance(Long userId, BigDecimal amount, Long bookingId, String idempotencyKey);

    /**
     * Add funds to wallet (customer deposit)
     * @param userId user ID
     * @param amount amount to add
     * @return updated Wallet entity
     * @throws com.marketplace.user.exception.UserNotFoundException if user not found
     */
    Wallet addFunds(Long userId, BigDecimal amount);

    /**
     * Validate if user has sufficient balance
     * @param userId user ID
     * @param amount amount to check
     * @return true if balance >= amount, false otherwise
     * @throws com.marketplace.user.exception.UserNotFoundException if user not found
     */
    boolean validateBalance(Long userId, BigDecimal amount);

    /**
     * Create wallet for new user
     * @param userId user ID
     * @return new Wallet entity
     */
    Wallet createWallet(Long userId);
}
