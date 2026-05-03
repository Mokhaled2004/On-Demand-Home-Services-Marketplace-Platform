package com.marketplace.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Request to create a new service category
 * Used by: POST /admin/categories
 */
@Data
public class CreateCategoryRequest {

    /**
     * Category name (e.g., "Plumbing", "Carpentry")
     * Must be unique (case-insensitive)
     */
    @NotBlank(message = "Category name is required")
    @Size(min = 2, max = 255, message = "Category name must be between 2 and 255 characters")
    private String name;

    /**
     * Category description
     * Optional field for additional details
     */
    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;
}
