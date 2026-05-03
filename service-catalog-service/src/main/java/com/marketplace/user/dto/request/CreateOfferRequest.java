package com.marketplace.user.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Request to create a new service offer
 * Used by: POST /provider/offers
 * Provider ID is extracted from JWT token (not from request body)
 */
@Data
public class CreateOfferRequest {

    /**
     * Service category ID
     * Must reference an existing category
     */
    @NotNull(message = "Category ID is required")
    private Long categoryId;

    /**
     * Service offer title (e.g., "Pipe Repair", "Kitchen Renovation")
     */
    @NotBlank(message = "Title is required")
    @Size(min = 3, max = 255, message = "Title must be between 3 and 255 characters")
    private String title;

    /**
     * Detailed description of the service
     * What's included, what to expect, etc.
     */
    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    private String description;

    /**
     * Service price in USD
     * Must be positive (> 0)
     */
    @NotNull(message = "Price is required")
    @Positive(message = "Price must be greater than 0")
    @DecimalMax(value = "999999.99", message = "Price must not exceed 999999.99")
    private BigDecimal price;

    /**
     * Start of availability window (ISO-8601 format)
     * Example: "2026-05-15T09:00:00"
     */
    @NotNull(message = "Available from date is required")
    private LocalDateTime availableFrom;

    /**
     * End of availability window (ISO-8601 format)
     * Must be after availableFrom
     * Example: "2026-05-15T17:00:00"
     */
    @NotNull(message = "Available to date is required")
    private LocalDateTime availableTo;
}
