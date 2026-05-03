package com.marketplace.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Request to update an existing service category
 * Used by: PUT /admin/categories/{categoryId}
 */
@Data
public class UpdateCategoryRequest {

    /**
     * Updated category name
     * Must be unique (case-insensitive), excluding current category
     */
    @NotBlank(message = "Category name is required")
    @Size(min = 2, max = 255, message = "Category name must be between 2 and 255 characters")
    private String name;

    /**
     * Updated category description
     */
    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;
}
