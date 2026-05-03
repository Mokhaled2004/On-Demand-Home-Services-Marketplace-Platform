package com.marketplace.user.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * Response containing service availability information
 * Returned by: GET /services/{offerId}/availability
 */
@Data
public class AvailabilityResponse {

    /**
     * Start of availability window
     */
    private LocalDateTime availableFrom;

    /**
     * End of availability window
     */
    private LocalDateTime availableTo;

    /**
     * Offer status: ACTIVE or INACTIVE
     */
    private String status;

    /**
     * Whether the service is currently available
     * true if status=ACTIVE and availableFrom <= NOW() <= availableTo
     */
    private Boolean isAvailable;
}
