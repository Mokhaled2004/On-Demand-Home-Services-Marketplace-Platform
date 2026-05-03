package com.marketplace.user.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Request to update an existing service offer
 * Used by: PUT /provider/offers/{offerId}
 * Provider can only update their own offers
 */
@Data
public class UpdateOfferRequest {

    /**
     * Updated service title
     */
    @NotBlank(message = "Title is required")
    @Size(min = 3, max = 255, message = "Title must be between 3 and 255 characters")
    private String title;

    /**
     * Updated service description
     */
    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    private String description;

    /**
     * Updated price
     * Must be positive (> 0)
     */
    @NotNull(message = "Price is required")
    @Positive(message = "Price must be greater than 0")
    @DecimalMax(value = "999999.99", message = "Price must not exceed 999999.99")
    private BigDecimal price;

    /**
     * Updated availability start
     */
    @NotNull(message = "Available from date is required")
    private LocalDateTime availableFrom;

    /**
     * Updated availability end
     * Must be after availableFrom
     */
    @NotNull(message = "Available to date is required")
    private LocalDateTime availableTo;

    /**
     * Offer status: ACTIVE or INACTIVE
     * ACTIVE = available for booking
     * INACTIVE = not available (soft delete)
     */
    @NotNull(message = "Status is required")
    @Pattern(regexp = "ACTIVE|INACTIVE", message = "Status must be ACTIVE or INACTIVE")
    private String status;
}
