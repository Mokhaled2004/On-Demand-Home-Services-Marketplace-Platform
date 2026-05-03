package com.marketplace.user.dto.response;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Generic API response wrapper
 * All endpoints return this format
 * 
 * Example success response:
 * {
 *   "status": "SUCCESS",
 *   "message": "Category created successfully",
 *   "data": { ... },
 *   "timestamp": "2026-05-02T10:00:00"
 * }
 * 
 * Example error response:
 * {
 *   "status": "ERROR",
 *   "message": "Category not found",
 *   "data": null,
 *   "timestamp": "2026-05-02T10:00:00"
 * }
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {

    /**
     * "SUCCESS" if request succeeded, "ERROR" if failed
     */
    private String status;

    /**
     * Human-readable message
     * Success: "Category created successfully"
     * Error: "Category not found"
     */
    private String message;

    /**
     * Response data (generic type T)
     * For success: contains the actual data
     * For error: null
     */
    private T data;

    /**
     * ISO-8601 timestamp when response was generated
     */
    private LocalDateTime timestamp;

    /**
     * Helper method to build success response with data only
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>("SUCCESS", null, data, LocalDateTime.now());
    }

    /**
     * Helper method to build success response with message and data
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>("SUCCESS", message, data, LocalDateTime.now());
    }

    /**
     * Helper method to build error response
     */
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>("ERROR", message, null, LocalDateTime.now());
    }
}
