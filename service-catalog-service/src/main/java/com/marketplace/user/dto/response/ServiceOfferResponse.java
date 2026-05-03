package com.marketplace.user.dto.response;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response containing service offer details
 * Returned by: GET /services, POST /provider/offers, etc.
 */
@Data
public class ServiceOfferResponse {

    /**
     * Unique offer ID (auto-generated)
     */
    private Long id;

    /**
     * Provider ID (from User Service)
     * Links to users.id in user_service_db
     */
    private Long providerId;

    /**
     * Category ID
     * Links to service_categories.id
     */
    private Long categoryId;

    /**
     * Service title (e.g., "Pipe Repair")
     */
    private String title;

    /**
     * Service description
     */
    private String description;

    /**
     * Service price in USD
     */
    private BigDecimal price;

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
     * When the offer was created
     */
    private LocalDateTime createdAt;

    /**
     * When the offer was last updated
     */
    private LocalDateTime updatedAt;
}
