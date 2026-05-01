package com.marketplace.user.repository;

import com.marketplace.user.entity.CompensationLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for CompensationLog entity
 * Provides data access methods for compensation log operations
 */
@Repository
public interface CompensationLogRepository extends JpaRepository<CompensationLog, Long> {

    /**
     * Find all compensation logs for a booking
     * @param bookingId the booking ID (from booking_db) to search for
     * @return List of compensation logs for the booking
     */
    List<CompensationLog> findByBookingId(String bookingId);

    /**
     * Find all compensation logs for a user
     * @param userId the user ID to search for
     * @return List of compensation logs for the user
     */
    List<CompensationLog> findByUserId(Long userId);

    /**
     * Find compensation logs by user ID and status
     * @param userId the user ID to search for
     * @param status the compensation status to filter by
     * @return List of compensation logs matching the criteria
     */
    List<CompensationLog> findByUserIdAndStatus(Long userId, CompensationLog.CompensationStatus status);

    /**
     * Find compensation logs by booking ID and status
     * @param bookingId the booking ID (from booking_db) to search for
     * @param status the compensation status to filter by
     * @return List of compensation logs matching the criteria
     */
    List<CompensationLog> findByBookingIdAndStatus(String bookingId, CompensationLog.CompensationStatus status);
}
