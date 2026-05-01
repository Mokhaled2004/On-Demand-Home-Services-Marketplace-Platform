package com.marketplace.user.util;

import com.marketplace.user.dto.response.ApiResponse;

/**
 * Utility wrapper around ApiResponse static factory methods.
 * Kept for backward compatibility - prefer using ApiResponse.success() / ApiResponse.error() directly.
 */
public final class ApiResponseBuilder {

    private ApiResponseBuilder() {}

    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.success(data);
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.success(message, data);
    }

    public static ApiResponse<Void> success(String message) {
        return ApiResponse.success(message, null);
    }

    public static ApiResponse<Void> error(String message) {
        return ApiResponse.error(message);
    }

    public static <T> ApiResponse<T> error(String message, T data) {
        return new ApiResponse<>(Constants.STATUS_ERROR, message, data, java.time.LocalDateTime.now());
    }
}
