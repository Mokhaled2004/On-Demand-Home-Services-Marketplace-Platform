package com.marketplace.user.service.compensation;

import com.marketplace.user.entity.CompensationLog;

import java.math.BigDecimal;
import java.util.List;

/**
 * Compensation Service Interface
 * Defines business logic for compensation tracking
 */
public interface CompensationService {

    /**
     * Log a deduction from wallet
     * @param bookingId booking ID
     * @param userId user ID
     * @param transactionId wallet transaction ID
     * @param amount amount deducted
     * @return saved CompensationLog entity
     */
    CompensationLog logDeduction(Long bookingId, Long userId, Long transactionId, BigDecimal amount);

    /**
     * Log a refund to wallet
     * @param bookingId booking ID
     * @param userId user ID
     * @param transactionId wallet transaction ID
     * @param amount amount refunded
     * @return saved CompensationLog entity
     */
    CompensationLog logRefund(Long bookingId, Long userId, Long transactionId, BigDecimal amount);

    /**
     * Get compensation history for user
     * @param userId user ID
     * @return List of CompensationLog entries for the user
     */
    List<CompensationLog> getCompensationHistory(Long userId);

    /**
     * Get compensation history for booking
     * @param bookingId booking ID
     * @return List of CompensationLog entries for the booking
     */
    List<CompensationLog> getBookingCompensationHistory(Long bookingId);

    /**
     * Get pending compensation logs for user
     * @param userId user ID
     * @return List of pending CompensationLog entries
     */
    List<CompensationLog> getPendingCompensations(Long userId);
}
