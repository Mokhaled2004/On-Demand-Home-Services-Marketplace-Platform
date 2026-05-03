package com.marketplace.user.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * Response containing service category details
 * Returned by: GET /admin/categories, POST /admin/categories, etc.
 */
@Data
public class ServiceCategoryResponse {

    /**
     * Unique category ID (auto-generated)
     */
    private Long id;

    /**
     * Category name (e.g., "Plumbing")
     */
    private String name;

    /**
     * Category description
     */
    private String description;

    /**
     * When the category was created
     */
    private LocalDateTime createdAt;

    /**
     * When the category was last updated
     */
    private LocalDateTime updatedAt;
}
